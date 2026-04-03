package flcr.backend.common.constants;

public class ResultCode {

    //成功
    public static int SUCCESS = 200;

    //参数错误
    public static int PARAM_ERROR = 400;

    //用户不存在
    public static int USER_NOT_EXIST = 401;

    //权限不足
    public static int PERMISSION_ERROR = 403;

    //用户已存在
    public static int USER_EXIST = 402;

    //资源不存在
    public static int RESOURCE_NOT_EXIST = 404;

    //系统错误
    public static int SYSTEM_ERROR = 500;

    //微信code无效
    public static int WX_CODE_ERROR = 1001;

    //微信接口调用失败
    public static int WX_API_ERROR = 1002;

    //手机号获取失败
    public static int PHONE_ERROR = 1003;
}
