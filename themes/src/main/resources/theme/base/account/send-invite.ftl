<#import "template.ftl" as layout>
<@layout.mainLayout active='invitations' bodyClass='invitations'; section>

<div class="row">
    <div class="col-md-10">
        <h2>${msg("sendInviteHtmlTitle")}</h2>
    </div>
</div>
    <form action="${url.sendInviteUrl}" class="form-horizontal" method="post">
        <div class="form-group">
            <div class="col-sm-2 col-md-2">
                <label for="email" class="control-label">${msg("inviteEmail")}</label>
            </div>

            <div class="col-sm-10 col-md-10">
                <input type="text" class="form-control" id="email" name="email" autofocus>
            </div>
        </div>

        <input type="hidden" id="stateChecker" name="stateChecker" value="${stateChecker}">
        <div class="form-group">
            <div id="kc-form-buttons" class="col-md-offset-2 col-md-10 submit">
                <div class="">
                    <button type="submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="submitAction" value="Send Invite">${msg("doSendInvite")}</button>
                </div>
            </div>
        </div>
    </form>
</@layout.mainLayout>