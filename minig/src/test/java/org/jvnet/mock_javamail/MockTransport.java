package org.jvnet.mock_javamail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * Mock {@link Transport} to deliver to {@link Mailbox}.
 *
 * @author Kohsuke Kawaguchi
 * @author dev@sokol-web.de <Kamill Sokol>
 */
public class MockTransport extends Transport {

    public MockTransport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public void connect(String host, int port, String user, String password) throws MessagingException {
        // noop
    }

    public void sendMessage(Message msg, Address[] addresses) throws MessagingException {
        for (Address a : addresses) {
            // create a copy to isolate the sender and the receiver

            Mailbox mailbox = Mailbox.get(a, "INBOX"); //MailboxHolder.getFixture(a, "INBOX");

            if (mailbox == null) {
                Mailbox inbox = new Mailbox(a, "INBOX");
                inbox.existsNow();

                inbox.add(msg);

                Mailbox.mailboxes.add(inbox);
            } else {
                if (mailbox.isError()) {
                    throw new MessagingException("Simulated error sending message to " + a);
                }
            }
        }
    }
}
