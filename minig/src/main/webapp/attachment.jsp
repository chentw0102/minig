<table cellspacing="0" cellpadding="0">
    <tbody>
        <tr ng-repeat="attachment in attachments">
            <td align="left" style="vertical-align: top;">
                <table>
                    <colgroup>
                        <col>
                    </colgroup>
                    <tbody>
                        <tr>
                            <td rowspan="2">
                                <img src="resources/images/mime.gif" class="gwt-Image">
                            </td>
                            <td colspan="3">
                                <div class="gwt-HTML">
                                    <b>{{attachment.fileName}}</b>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="gwt-Label">{{attachment.size | prettyPrintSize}}</div>
                            </td>
                            <td>
                                <a class="gwt-Anchor" ng-href="api/1/attachment/{{attachment.id}}?download=true" target="_blank">Download</a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    </tbody>
</table>