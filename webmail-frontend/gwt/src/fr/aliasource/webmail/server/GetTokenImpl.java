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

package fr.aliasource.webmail.server;

import fr.aliasource.webmail.client.rpc.GetToken;

/**
 * Logout Ajax call implementation
 * 
 * @author tom
 * 
 */
public class GetTokenImpl extends SecureAjaxCall implements GetToken {

	private static final long serialVersionUID = 704950311648312987L;

	public String getToken() {
		return getAccount().getToken();
	}

}
