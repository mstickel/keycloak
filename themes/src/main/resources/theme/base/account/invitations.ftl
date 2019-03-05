<#import "template.ftl" as layout>
<@layout.mainLayout active='invitations' bodyClass='invitations'; section>

<div class="row">
    <div class="col-md-10">
        <h2>${msg("invitationsHtmlTitle")}</h2>
    </div>
</div>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <td align="right" colspan="4">
                <form action="${url.sendInviteUrl}" method="get">
                    <input type="hidden" id="stateChecker" name="stateChecker" value="${stateChecker}">
                    <button id="send-new-invite" class="btn btn-default">${msg("sendNewInvite")}</button>
                </form>
            </td>
        </tr>
        <tr>
            <td>${msg("invitationSentTo")}</td>
            <td>${msg("date")}</td>
            <td>${msg("status")}</td>
            <td>${msg("action")}</td>
        </tr>
        </thead>
        <tbody>
            <#list invitations.invitations as invitation>
                <tr>
                    <td>${invitation.sentToEmail}</td>
                    <td>${invitation.date?datetime}</td>
                    <td>${invitation.status}</td>
                    <td>Rescind</td>
                </tr>
            </#list>
        </tbody>
    </table>

</@layout.mainLayout>