package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.TenantBill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailWithInvoiceService {
    private final JavaMailSender mailSender;
    private final InvoicePdfGenerator pdfGenerator;
    private final SmsService smsService;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Async
    public void sendBillPaidEmail(TenantBill bill, String tenantEmail, String adminEmail, String tenantPhone) {
        try {
            byte[] pdfBytes = pdfGenerator.generateInvoicePdf(bill, "PaidDate", LocalDate.now());
            MimeMessage mimeMsg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(new String[]{tenantEmail, adminEmail});
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
            String smsMessage = "Your bill for " + bill.getMonthYear() + " has been paid. Thank you!";
            smsService.sendSms(tenantPhone, smsMessage);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(EmailWithInvoiceService.class)
                .error("Error in sendBillPaidEmail: {}", ex.getMessage(), ex);
        }
    }

    @Async
    public void notifyBillGenerated(TenantBill bill, String tenantEmail, String month, String tenantPhone) {
        try {
            LocalDate createdDate = bill.getCreatedDate();
            byte[] pdfBytes = pdfGenerator.generateInvoicePdf(bill, "CreatedDate", createdDate);
            MimeMessage mimeMsg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(tenantEmail);
            helper.setSubject("Bill Generated for " + month);
            String body = String.format(
                "Hi %s,%n%nYour bill for %s has been generated.%n" +
                "Rent: %.2f%nWater: %.2f%nElectricity: %.2f%nTotal: %.2f%n%nThank you.",
                bill.getTenantName(),
                month,
                bill.getRent(),
                bill.getWater(),
                bill.getElectricity(),
                bill.getRent() + bill.getWater() + bill.getElectricity()
            );
            helper.setText(body);
            helper.addAttachment("Invoice_" + bill.getMonthYear() + ".pdf",
                () -> new java.io.ByteArrayInputStream(pdfBytes),
                "application/pdf");
            mailSender.send(mimeMsg);
            String smsMessage = "Your bill for " + month + " has been generated.";
            smsService.sendSms(tenantPhone, smsMessage);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(EmailWithInvoiceService.class)
                .error("Error in notifyBillGenerated: {}", ex.getMessage(), ex);
        }
    }
}