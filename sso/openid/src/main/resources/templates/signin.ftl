<#import "boilerplate.ftl" as bp>

<@bp.html>
<#if authReq??>
<#assign params = authReq.getParameterMap()>
<body>
    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span12 c-sso-consumer-signin-post">
                <img src="../../scripts/cdeio/themes/default/assets/images/loading.gif" />
                <div>正在向单点登录服务器发送请求，请稍候...</div>
            </div>
        </div>
    </div>
    <form name="openidRedirectionFrom" action="${authReq.getOPEndpoint()}" method="post" accept-charset="UTF-8">
        <#list params?keys as key>
            <input type="hidden" name="${key}" value="${params[key]}" />
        </#list>
            <input type="hidden" name="applicationName" value="${applicationName}" />
            <input type="hidden" name="indexUrl" value="${indexUrl}" />
            <input type="hidden" name="signOutUrl" value="${signOutUrl}" />
    </form>
    <script>
        document.forms.openidRedirectionFrom.submit();
    </script>
</body>
</#if>
</@bp.html>
