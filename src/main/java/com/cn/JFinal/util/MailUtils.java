package com.cn.JFinal.util;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;
import java.io.File;
import java.util.*;

public class MailUtils {

	/**
	 * 
	 * @param host
	 *            smtp服务器
	 * @param from
	 *            发件人地址
	 * @param pwd
	 *            发件人密码
	 * @param toList
	 *            收件人地址
	 * @param subject
	 *            邮件标题
	 * @param text
	 *            邮件内容
	 * @param fileList
	 *            附件
	 * @return boolean(成功：true，失败：false)
	 */
	@SuppressWarnings({ "finally" })
	public static boolean sendMultipleEmail(String host, String from, String pwd, List<String> toList, String subject,
			String text, List<File> fileList) {
		boolean b = false;
		try {
			/*
			 * 1. 创建参数配置, 用于连接邮件服务器的参数配置
			 */
			Properties props = new Properties(); // 参数配置
			props.setProperty("mail.debug", "true"); // 开启debug调试
			props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
			props.setProperty("mail.host", host); // 发件人的邮箱的 SMTP 服务器地址
			props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

			/*
			 * 2. 根据配置创建会话对象, 用于和邮件服务器交互
			 */
			Session session = Session.getInstance(props); // 设置环境信息
			// session.setDebug(true); // 设置为debug模式, 可以查看详细的发送 log

			/*
			 * 3. 创建一封邮件
			 */
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from, "", "UTF-8"));// From: 发件人
			for (String to : toList) {
				msg.addRecipient(RecipientType.TO, new InternetAddress(to, "", "UTF-8")); // To: 收件人（可以增加多个收件人、抄送、密送）
			}
			msg.setSubject(subject); // Subject: 邮件主题

			/*
			 * 下面是邮件内容的创建:
			 */
			MimeBodyPart content = new MimeBodyPart(); // 内容节点
			content.setContent(text, "text/html;charset=UTF-8");

			// 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
			MimeMultipart mm = new MimeMultipart();
			mm.addBodyPart(content);
			for (File file : fileList) {
				MimeBodyPart attachment = new MimeBodyPart();
				DataHandler dh2 = new DataHandler(new FileDataSource(file)); // 读取本地文件
				attachment.setDataHandler(dh2); // 将附件数据添加到“节点”
				attachment.setFileName(MimeUtility.encodeText(dh2.getName())); // 设置附件的文件名（需要编码）
				mm.addBodyPart(attachment); // 如果有多个附件，可以创建多个多次添加
			}
			mm.setSubType("mixed"); // 混合关系

			msg.setContent(mm); // 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
			msg.setSentDate(new Date()); // 设置发件时间
			msg.saveChanges(); // 保存上面的所有设置

			/*
			 * 4. 根据 Session 获取邮件传输对象
			 */
			Transport transport = session.getTransport();

			/*
			 * 5. 使用 邮箱账号 和 密码 连接邮件服务器 这里认证的邮箱必须与 message 中的发件人邮箱一致，否则报错
			 */
			transport.connect(from, pwd);

			/*
			 * 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人,
			 * 密送人
			 */
			transport.sendMessage(msg, msg.getAllRecipients());

			/*
			 * 7. 关闭连接
			 */
			transport.close();

			b = true;
		} catch (Exception e) {
			// 异常捕捉
		} finally {
			return b;
		}

	}

	public static void main(String[] args) {
		// maven 包引入
		/*<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-email</artifactId>
			<version>1.5</version>
		</dependency>*/
		String host = "smtp.139.com";
		String from = "14759111800@139.com";
		String pwd = "panda1985";
		List<String> toList = new ArrayList<String>();
		toList.add("850180541@qq.com");
		toList.add("769310558@qq.com");
		List<File> fileList = new ArrayList<File>();
		// fileList.add(new File("C:\\Users\\HWJ\\Desktop\\Antenna\\新建 Microsoft Excel
		// 工作表.xlsx"));
		// fileList.add(new File("C:\\Users\\HWJ\\Desktop\\Antenna\\集中信息导入模板
		// (5).xlsx"));
		String subject = "测试标题";
		String text = "测试内容";

		MailUtils.sendMultipleEmail(host, from, pwd, toList, subject, text, fileList);
	}
}