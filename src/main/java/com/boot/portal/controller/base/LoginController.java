package com.boot.portal.controller.base;/**
 * @description
 * @autor xbwu on 2018/1/22.
 */

import com.boot.portal.common.config.WebSecurityConfig;
import com.boot.portal.common.base.ResultWapper;
import com.boot.portal.common.util.LocaleMessageSource;
import com.boot.portal.common.util.MD5Util;
import com.boot.portal.entity.portal.user.User;
import com.boot.portal.service.user.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Locale;

/**
 * 登录
 * @author xbwu
 * @create 2018-01-22 
 **/
//默认所有类开启@ResponeseBody ，如需要返回视图则需要注明返回类为ModelAndView
@RestController
//在控制器中可以直接书写参数的校验规则和返回消息
@Validated
public class LoginController extends BaseController{
    @Autowired
    @Qualifier("localJdbcTemplate")
    JdbcTemplate jdbcTemplate1;
    /**
     * 国际化消息
     */
    @Autowired
    private LocaleMessageSource msg;
    @Autowired
    UserService userService;

    @GetMapping(value = "/login")
    public ModelAndView login() throws Exception {
        ModelAndView mv = getMV("login");
        return mv;
    }
    @PostMapping(value = "/login")
    public ResultWapper login(Model model,
                              @NotBlank(message = "用户名不能为空") String username,
                              @NotBlank(message = "密码不能为空") String password,
                              HttpServletRequest request) throws Exception {
        User ue = userService.findByUserAccount(username.trim());
        ResultWapper rw = null;
        do {
            if (ue == null) {
                rw = ResultWapper.error(msg.getMsg("login.incorrect.username.or.password"));
                break;
            }
            if (!ue.getValid()) {
                rw = ResultWapper.error(msg.getMsg("login.incorrect.username.disable"));
                break;
            }
            String dbpd = ue.getPassword();
            String nowpd = MD5Util.getMD5(password.trim());
            if (!dbpd.equals(nowpd)) {
                rw = ResultWapper.error("用户或密码错误");
                break;
            }
            rw=ResultWapper.success("登录成功");
            rw.addResult("ue",ue);
            WebUtils.setSessionAttribute(request,WebSecurityConfig.SESSION_KEY,ue);
        } while (false);
        return rw;
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 移除session
        session.removeAttribute(WebSecurityConfig.SESSION_KEY);
        return "redirect:/login";
    }
}
