package com.egatetutor.backend.controller;

import com.egatetutor.backend.model.CoursesOffered;
import com.egatetutor.backend.model.Payment;
import com.egatetutor.backend.model.SubscriptionDetails;
import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.model.responsemodel.CreateOrder;
import com.egatetutor.backend.model.responsemodel.PurchaseCourse;
import com.egatetutor.backend.repository.CoursesOfferedRepository;
import com.egatetutor.backend.repository.PaymentRepository;
import com.egatetutor.backend.repository.SubscriptionRepository;
import com.egatetutor.backend.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/payment")
public class PaymentController {

    RazorpayClient razorpay;
    @Autowired
    private Environment env;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CoursesOfferedRepository coursesOfferedRepository;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    private static String keyId;
    private  static String keySecret;
    public void init() throws RazorpayException {
        keyId = env.getProperty("key_id");
        keySecret = env.getProperty("key_secret");
        razorpay = new RazorpayClient(keyId, keySecret);
    }
    @PostMapping("/createOrder")
    public ResponseEntity<String> createOrder(@Valid @RequestBody CreateOrder createOrder) throws RazorpayException{
             init();
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", createOrder.getAmount()); // amount in the smallest currency unit
            orderRequest.put("currency", createOrder.getCurrency());
            orderRequest.put("receipt", createOrder.getReceipt());
            orderRequest.put("payment_capture", 1);
            Order order =  razorpay.Orders.create(orderRequest);
            String result= order.get("id");
            orderRequest.put("orderId",result);
            return ResponseEntity.status(HttpStatus.OK).body(orderRequest.toString());
    }

    @PostMapping("/verifiedPayment")
    public ResponseEntity verifiedPayment(@Valid @RequestBody PurchaseCourse purchaseCourse)
    {
        JSONObject options = new JSONObject();

        if (StringUtils.isNotBlank(purchaseCourse.getPaymentId()) && StringUtils.isNotBlank(purchaseCourse.getSignature())
                && StringUtils.isNotBlank(purchaseCourse.getOrderId())) {
            try {
                        options.put("razorpay_payment_id", purchaseCourse.getPaymentId());
                options.put("razorpay_order_id", purchaseCourse.getOrderId());
                options.put("razorpay_signature", purchaseCourse.getSignature());
                boolean isEqual = Utils.verifyPaymentSignature(options, keySecret);
                if (isEqual) {
                    Payment payment = new Payment();
                    payment.setOrderId(purchaseCourse.getOrderId());
                    payment.setPaymentId(purchaseCourse.getPaymentId());
                    Optional<UserInfo> userInfo = userRepository.findById(purchaseCourse.getUserId());
                    userInfo.ifPresent(payment::setUserId);
                    userInfo.ifPresent(info -> {
                        try {
                            addNewSubscription(info, purchaseCourse.getExamId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    paymentRepository.save(payment);
                    return ResponseEntity.status(HttpStatus.OK).build();
                }
            } catch (RazorpayException e) {
                System.out.println("Exception caused because of " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private void addNewSubscription(UserInfo userInfo, List<String> examIdList) throws Exception{
        List<CoursesOffered> coursesOfferedList = coursesOfferedRepository.findByExamCodeList(examIdList);
        if( coursesOfferedList == null || coursesOfferedList.isEmpty()) throw new Exception("Courses Offered doesn't exist");
        for(CoursesOffered coursesOffered: coursesOfferedList){
            if(coursesOffered.getExamNameService().equals("COMPLETE PACKAGE")){
               List<CoursesOffered> coursesOfferedIndividualList =  coursesOfferedRepository.findCoursesForCompletePackage(coursesOffered.getExamName(), coursesOffered.getBranch());
                if( coursesOfferedIndividualList == null || coursesOfferedIndividualList.isEmpty()) throw new Exception("Courses Offered doesn't exist");
               for(CoursesOffered cc: coursesOfferedIndividualList){
                   if(cc.getId().equals(coursesOffered.getId())) continue;
                    SubscriptionDetails subscriptionDetails = new SubscriptionDetails();
                    subscriptionDetails.setUserId(userInfo);
                    subscriptionDetails.setCourseOfferedId(cc);
                    subscriptionRepository.save(subscriptionDetails);
                }

            }else {
                SubscriptionDetails subscriptionDetails = new SubscriptionDetails();
                subscriptionDetails.setCourseOfferedId(coursesOffered);
                subscriptionDetails.setUserId(userInfo);
                subscriptionRepository.save(subscriptionDetails);
            }

        }


    }

}
