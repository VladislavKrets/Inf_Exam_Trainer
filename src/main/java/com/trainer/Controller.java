package com.trainer;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
public class Controller {
    private int id = 1;
    private int maxId = 1;
    private DAO dao;
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView index(Model model) {
        ModelAndView modelAndView = new ModelAndView("index");
        if (dao == null) dao = new DAO();
        if (maxId == 1) maxId = dao.getMaxId();
        if (id == maxId) {
            id = 1;
            return new ModelAndView("complete"); //todo
        }
        model.addAttribute("question", String.format("%s", dao.getQuestion(id)));
        List<Answer> answers = dao.getAnswers(id);
        String trueAnswer = trueAnswer(answers);

        model.addAttribute("answer", String.format("<p><input name=\"dzen\" type=\"radio\" id=\"first\">%s</p>\n" +
                "                <p><input name=\"dzen\" type=\"radio\"id=\"second\">%s</p>\n" +
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
        id++;
        return modelAndView;
    }

    @RequestMapping(value = "/reboot", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            id = 1;
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
