package com.trainer;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
    private static Class<Main> application = Main.class;
    public static Connection connection;
    public static Statement statement;

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(application);
    }

    private static void parseBase() throws ClassNotFoundException, IOException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String fileName = "exam";
        InputStream fis = new FileInputStream(fileName);
        POIFSFileSystem fs = new POIFSFileSystem(fis);
        HWPFDocument doc = new HWPFDocument(fs);

        Range range = doc.getRange();
        Paragraph tablePar = range.getParagraph(0);
        if (tablePar.isInTable()) {
            Table table = range.getTable(tablePar);
            addToDb(table);
        }
    }


    private static void addToDb(Table table) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:dbExam");
        statement = connection.createStatement();
        ResultSet resultSet;
        TableRow row;
        Paragraph paragraph;
        int id = -1;
        for (int rowIdx = 0; rowIdx < table.numRows(); rowIdx++) {
            row = table.getRow(rowIdx);
            paragraph = row.getCell(1).getParagraph(0);
            statement.execute(String.format("insert into \'Questions\' (\'data\') values (\"%s\");", paragraph.text().trim()));
            resultSet = statement.executeQuery("select last_insert_rowid();");
            id = resultSet.getInt(1);
            paragraph = row.getCell(2).getParagraph(0);
            isRedToDb(paragraph, id);
            paragraph = row.getCell(2).getParagraph(1);
            isRedToDb(paragraph, id);
            paragraph = row.getCell(2).getParagraph(2);
            isRedToDb(paragraph, id);
            paragraph = row.getCell(2).getParagraph(3);
            isRedToDb(paragraph, id);
        }
        statement.close();
    }

    private static void isRedToDb(Paragraph paragraph, int questionId) throws SQLException {
        if (isRedParagraph(paragraph)) {
            statement.execute(String.format("insert into \'Answers\' (\'question_id\', \'data\', \'is_true\') values (%d, \"%s\", %d);",
                    questionId, paragraph.text().trim(), 1));
        }
        else {
            statement.execute(String.format("insert into \'Answers\' (\'question_id\', \'data\', \'is_true\') values (%d, \"%s\", %d);",
                    questionId, paragraph.text().trim(), 0));;
        }
    }

    private static void printRedPlus(Paragraph paragraph) {
        if (isRedParagraph(paragraph)) {
            System.out.println(paragraph.text().trim() + " +");
        }
        else {
            System.out.println(paragraph.text().trim());
        }
    }

    private static boolean isRedParagraph(Paragraph paragraph) {
        CharacterRun characterRun;
        int j = 0;
        while (true) {
            characterRun = paragraph.getCharacterRun(j++);
            if (characterRun.getColor() == 6) {
                return true;
            }
            if (characterRun.getEndOffset() == paragraph.getEndOffset()){
                j = 0;
                break;
            }
        }
        return false;
    }
}

