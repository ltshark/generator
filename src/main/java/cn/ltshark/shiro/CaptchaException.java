package cn.ltshark.shiro;

import org.apache.shiro.authc.AuthenticationException;

/**
 * Created by ltshark on 15/6/7.
 */
public class CaptchaException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public CaptchaException() {

        super();

    }

    public CaptchaException(String message, Throwable cause) {

        super(message, cause);

    }

    public CaptchaException(String message) {

        super(message);

    }

    public CaptchaException(Throwable cause) {

        super(cause);

    }

}
