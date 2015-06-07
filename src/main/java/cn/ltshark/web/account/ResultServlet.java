package cn.ltshark.web.account;

/**
 * Created by surfrong on 2015/6/3.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class ResultServlet {
    private Logger logger = LoggerFactory.getLogger(ResultServlet.class);
    @RequestMapping(value="resultServlet/validateCode",method=RequestMethod.POST)
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        response.setContentType("text/html;charset=utf-8");
        String validateC = (String) request.getSession().getAttribute(VerifyCodeServlet.KEY_VALIDATE_CODE);
        logger.info(validateC);
        String veryCode = request.getParameter("c");
        logger.info(veryCode);
        PrintWriter out = response.getWriter();
        if(veryCode==null||"".equals(veryCode)){
            out.println("验证码为空");
        }else{
            if(validateC.equalsIgnoreCase(veryCode)){
                out.println("true");
            }else{
                out.println("false");
            }
        }
        out.flush();
        out.close();
    }
}