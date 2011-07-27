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

package fr.aliasource.webmail.common.imap;

public class StoreException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5242149136815514452L;

	public StoreException(String s, Throwable e) {
		super(s, e);
	}

	public StoreException(String string) {
		super(string);
	}

}
