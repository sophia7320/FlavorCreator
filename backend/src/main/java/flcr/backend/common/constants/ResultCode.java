package flcr.backend.common.constants;

public final class ResultCode {

    private ResultCode() {}

    // 成功
    public static final int SUCCESS = 200;
    // 参数错误
    public static final int PARAM_ERROR = 400;
    // 用户不存在
    public static final int USER_NOT_EXIST = 401;
    // 用户已存在
    public static final int USER_EXIST = 402;
    // 权限不足
    public static final int PERMISSION_ERROR = 403;
    // 资源不存在
    public static final int RESOURCE_NOT_EXIST = 404;
    // 系统错误
    public static final int SYSTEM_ERROR = 500;
    // 微信 code 无效
    public static final int WX_CODE_ERROR = 1001;
    // 微信接口调用失败
    public static final int WX_API_ERROR = 1002;
    // 手机号获取失败
    public static final int PHONE_ERROR = 1003;

    // 图片上传错误码
    public static final int IMAGE_TYPE_ERROR = 2001;
    public static final int IMAGE_SIZE_ERROR = 2002;
    public static final int IMAGE_MODERATION_FAILED = 2003;
    public static final int IMAGE_UPLOAD_ERROR = 2004;
    public static final int IMAGE_SCENE_ERROR = 2005;
}
