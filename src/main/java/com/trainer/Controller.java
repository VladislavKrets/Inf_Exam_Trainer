package com.trainer;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class Controller {
    private int infId = 1;
    private int maxInfId = 1;
    private int physId = 0;
    private  int maxPhysId = 0;
    private DAO dao;
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView index(Model model) {
        ModelAndView modelAndView = new ModelAndView("index");
        if (dao == null) dao = new DAO();
        if (maxInfId == 1) maxInfId = dao.getMaxId();
        if (infId == maxInfId) {
            infId = 1;
            return new ModelAndView("complete"); //todo
        }
        model.addAttribute("question", String.format("%s", dao.getQuestion(infId)));
        List<Answer> answers = dao.getAnswers(infId);
        String trueAnswer = trueAnswer(answers);

        model.addAttribute("answer", String.format("<p><input name=\"dzen\" type=\"radio\" id=\"first\">%s</p>\n" +
                "                <p><input name=\"dzen\" type=\"radio\" id=\"second\">%s</p>\n" +
                "                <p><input name=\"dzen\" type=\"radio\" id=\"third\">%s</p>\n" +
                "                <p><input name=\"dzen\" type=\"radio\" id=\"fourth\">%s</p>\n" +
                "                <div>\n" +
                "                    <p>\n" +
                "\n" +
                "                        <input type=\"button\" value=\"Выбрать\" class=\"submit\" onclick=\"\n" +
                "                            $(function click() {\n" +
                "                              $('.green').eq(0).remove();\n" +
                "                              $('.red').eq(0).remove();\n" +
                "                              if ($('#%s').prop('checked')){\n" +
                "                                $('#first').before('<p class=\\'green\\'>Верно</p>')\n" +
                "                              }\n" +
                "                              else{\n" +
                "                                  $('#first').before('<p class=\\'red\\'>Неверно</p>')\n" +
                "                              }\n" +
                "                            });\n" +
                "                        \">\n" +
                "                        <input type=\"submit\" value=\"Следующий\" class=\"submit\"/>\n" +
                "                    </p>\n" +
                "                </div>",
                answers.get(0).getData(), answers.get(1).getData(), answers.get(2).getData(), answers.get(3).getData(), trueAnswer));
        infId++;
        return modelAndView;
    }

    @RequestMapping(value = "/reboot", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            infId = 1;
            physId = 0;
            response.sendRedirect("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping(value = "/stop")
    public String stop(){
        try {
            dao.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.exit(0);
        return "";
    }
    @RequestMapping(value = "/physics", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView physics(Model model){
        ModelAndView modelAndView = new ModelAndView("physics");
        String[] fileNames = new File("result").list();
        assert fileNames != null;
        Arrays.sort(fileNames);
        if (maxPhysId == 0) maxPhysId = fileNames.length - 1;
        if (physId >= maxPhysId){
            physId = 0;
            return new ModelAndView("complete");
        }
        List<String> answers = results("answers.txt");
        String answer = getAnswer(answers, fileNames[physId]);
        String path0 = "/result_inv/" + fileNames[physId] + "/0.png";
        String path1 = "/result_inv/" + fileNames[physId] + "/1.png";
        String line = String.format("" +
                "\t<div class=\"answer\">\n" +
                "\t  <form action=\"/physics\">\n" +
                "\t\t<span>1</span>\n" +
                "\t\t<input name=\"zhopa\" type=\"radio\" value=\"1\" id=\"first\">\n" +
                "\t\t<span>2</span>\n" +
                "\t\t<input name=\"zhopa\" type=\"radio\" value=\"2\" id=\"second\">\n" +
                "\t\t<span>3</span>\n" +
                "\t\t<input name=\"zhopa\" type=\"radio\" value=\"3\" id=\"third\">\n" +
                "\t\t<span>4</span>\n" +
                "\t\t<input name=\"zhopa\" type=\"radio\" value=\"4\" id=\"fourth\">\n" +
                "\t\t<p>\n" +
                "\t\t  <input type=\"button\" value=\"Выбрать\" onclick=\"$(function click(){\n" +
                "\t\t\t$('.green').eq(0).remove();\n" +
                "\t\t\t$('.red').eq(0).remove();\n" +
                "\t\t\tif ($('#%s').prop('checked')){\n" +
                "            $('form').before('<p class=\\'green\\'>Верно</p>');\n" +
                "\t\t\t}\n" +
                "\t\t\telse{\n" +
                "            $('form').before('<p class=\\'red\\'>Неверно</p>');\n" +
                "\t\t\t} \n" +
                "\t\t\t});\n" +
                "\t\t\t\">\n" +
                "\t\t  <input type=\"submit\" value=\"Следующий\">\n" +
                "\t\t</p>\n" +
                "\t  </form>\n" +
                "\t</div>", getAnswer(answer.split("=")[1]));
        model.addAttribute("line", line);
        model.addAttribute("path0", path0);
        model.addAttribute("path1", path1);
        physId++;
       return modelAndView;
    }
    private String getAnswer(String answer){
        switch (answer) {
            case "1": return "first";
            case "2": return "second";
            case "3": return "third";
            case "4": return "fourth";
        }
        return "";
    }
    private List<String> results(String file) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    private String getAnswer(List<String> list, String startLine){
        for (String str : list) {
            if (str.startsWith(startLine + "=")){
                return str;
            }
        }
        return "";
    }
    private String trueAnswer(List<Answer> answers) {
        if (answers.get(0).isTrue()) {
            return  "first";
        }
        else if (answers.get(1).isTrue()) {
            return  "second";
        }
        else if (answers.get(2).isTrue()) {
            return  "third";
        }
        else if(answers.get(3).isTrue()){
            return  "fourth";
        }
        return "";
    }
}
