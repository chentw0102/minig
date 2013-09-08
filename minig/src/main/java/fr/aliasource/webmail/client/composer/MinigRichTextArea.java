/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.client.composer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SimplePanel;

import fr.aliasource.webmail.client.View;
import fr.aliasource.webmail.client.shared.IBody;
import fr.aliasource.webmail.client.test.BeanFactory;

public class MinigRichTextArea extends MinigTextArea {

    private RichTextArea rta;
    private IBody mailBody;
    private Boolean htmlBody;

    public MinigRichTextArea(View ui, MailComposer mc) {
        super(ui, mc);
        this.rta = new RichTextArea();
        this.htmlBody = false;
        // FIXME
        // http://code.google.com/p/google-web-toolkit/issues/detail?id=1052

        this.setHeight(Window.getClientHeight());
        SimplePanel sp = new SimplePanel();
        sp.add(rta);
        this.add(rta);
        this.mailBody = BeanFactory.instance.body().as();
    }

    public void addKeyboardListener(KeyPressHandler addKeyboardListener) {
        rta.addKeyPressHandler(addKeyboardListener);
    }

    public void setFocus(boolean b) {
        rta.setFocus(b);
    }

    public IBody getMailBody() {
        GWT.log("returning mailbody, signature: " + signature, null);

        mailBody.setHtml(rta.getHTML());
        IBody ret = BeanFactory.instance.body().as();
        ret.setHtml(mailBody.getHtml());
        return ret;
    }

    public void setHeight(int height) {
        // TODO
        if (height > 350) {

            rta.setHeight(height - 350 + "px");
        } else {
            rta.setHeight(height + "px");
        }
    }

    public void setMailBody(IBody body) {
        mailBody.setPlain(body.getPlain());
        if (body.getHtml() != null) {
            mailBody.setHtml(ensureHTML(body.getHtml()));
            this.htmlBody = true;
        } else {
            GWT.log("using plain", null);
            mailBody.setHtml(ensureHTML(body.getPlain()));
        }

        GWT.log("rta.setHTML:\n" + mailBody.getHtml(), null);
        rta.setHTML(mailBody.getHtml());
    }

    @Override
    public RichTextToolbar getRichTextToolbar() {
        return new RichTextToolbar(rta);
    }

    private String ensureHTML(String badHTML) {
        String sig = badHTML;
        sig = sig.replace("\r", "");
        sig = sig.replace("\n", "<br>");
        return sig;
    }

    public boolean isEmpty() {
        boolean ret = false;

        String cur = rta.getHTML().toLowerCase();
        cur = cur.replace("<br>", "");
        cur = cur.replace("<br/>", "");
        cur = cur.replace("--", "");
        cur = cur.replace(" ", "");

        String sig = "";
        if (signature != null) {
            sig = signature.toLowerCase();
            sig = sig.replace("--", "");
            sig = sig.replace(" ", "");
            sig = sig.replace("\n", "");
            sig = sig.replace("\r", "");
        }

        GWT.log("simplified content: " + cur, null);
        GWT.log("simplified sig: " + cur, null);

        if (cur.isEmpty() || cur.equals(sig)) {
            ret = true;
        }
        GWT.log("isEmpty, content: [" + rta.getHTML() + "] empty => " + ret, null);
        return ret;
    }

    public Boolean isHtmlBody() {
        return htmlBody;
    }

}
