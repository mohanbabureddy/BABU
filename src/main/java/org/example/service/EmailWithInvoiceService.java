package org.example.service;

import org.example.model.TenantBill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class EmailWithInvoiceService {

    private final JavaMailSender mailSender;
    private final InvoicePdfGenerator pdfGenerator;
    private final String fromAddress;

    public EmailWithInvoiceService(JavaMailSender mailSender,
                                   InvoicePdfGenerator pdfGenerator,
                                   @Value("${spring.mail.from}") String fromAddress) {
        this.mailSender    = mailSender;
        this.pdfGenerator  = pdfGenerator;
        this.fromAddress   = fromAddress;
    }

    @Async
    public void sendBillPaidEmail(TenantBill bill,
                                  String tenantEmail,
                                  String adminEmail) throws Exception {

        byte[] pdfBytes = pdfGenerator.generateInvoicePdf(bill);

        MimeMessage mimeMsg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(new String[]{ tenantEmail, adminEmail });
        helper.setSubject("Invoice: Bill Paid for " + bill.getMonthYear());

        String body = String.format(
                "Hi %s,%n%nYour bill for %s has been successfully paid. " +
                        "Please find the attached invoice for details.%n%nThank you!",
                bill.getTenantName(), bill.getMonthYear()
        );
        helper.setText(body);

        helper.addAttachment("Invoice_" + bill.getMonthYear() + ".pdf",
                () -> new java.io.ByteArrayInputStream(pdfBytes),
                "application/pdf");

        mailSender.send(mimeMsg);
    }
}
