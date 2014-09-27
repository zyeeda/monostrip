<#if dataType?? && dataType == "jsonp">
{success:true}
<#else>
<#import "boilerplate.ftl" as bp>

<@bp.html>
<body class="c-green-background">
    <div class="c-header">
        <div class="c-logo"></div>
    </div>
    <div class="container-fluid c-content">
        <div class="row-fluid">
            <div class="span5 offset1 hidden-phone c-sso-signout-left">
                <img src="../../scripts/cdeio/themes/default/assets/images/system-signout-logo.png" />
            </div>
            <div class="span4 c-sso-signout-right">
                <h2>系统成功退出!</h2>
                <div>您已安全退出${applicationName}，可以进行如下操作：</div>
                <br />
                <a class="btn btn-large btn-warning" href="${opSignOutUrl}"><i class="icon-lock icon-white c-icon-small"></i>&nbsp;&nbsp;退出全站</a>&nbsp;
                或者点击<a href="${signInUrl}" class="c-sso-consumer-signout-resignin">重新登录</a>
            </div>
        </div>
    </div>
</body>

</@bp.html>
</#if>
