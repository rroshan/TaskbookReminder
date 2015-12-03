import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {
	private String subject;
	private String body;
	private String from;
	private String recipient;

	public SendMail(String subject, String body, String from, String recipient)
	{
		this.subject = subject;
		this.body = body;
		this.from = from;
		this.recipient = recipient;
	}

	public void send()
	{
		Properties props = new Properties();

		props.setProperty("mail.host", "smtp.sendgrid.net");
		props.setProperty("mail.smtp.port", "25");
		props.setProperty("mail.smtp.auth", "true");

		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("roshan91", "ArchLinux@2015");
			}
		};

		Session session = Session.getDefaultInstance(props, auth);

		Message msg = new MimeMessage(session);

		try {
			msg.setSubject(subject);
			msg.setContent(body, "text/html");
			msg.setFrom(new InternetAddress(from, "Taskbook"));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

			Transport.send(msg);
		} catch (MessagingException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
