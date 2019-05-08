package com.a4sys.luciWS.amazonSMS;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.domain.ResponseWS;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmorones on 4/18/18.
 */
public class AmazonSMSService extends DaoService {

    public AmazonSMSService(){
        super(SystemConstants.JDNI);
    }


    /***
     * metodo que envia un sms via AWS
     * @param amazonSMSRequest
     * @return
     */
    public ResponseWS send(AmazonSMSRequest amazonSMSRequest) {

        ResponseWS response = new ResponseWS();
        response.setStatus("0");
        Long codigoPais = 0L;
        try {

            sqlQueryExecutor.addParameter("idWebService",amazonSMSRequest.getIdWebService());
            ALQueryResult data = sqlQueryExecutor.executeQueryUsingQueryString("select usuario, contrasenia, LLAVEENCRIPTAR from dicwebservices where idWebService = {idWebService} ");

            sqlQueryExecutor.cleanParameter();

            String ACCESS_KEY  = data.get(0,"CONTRASENIA").toString();
            String SECRET_KEY   = data.get(0,"LLAVEENCRIPTAR").toString();

            BasicAWSCredentials creds = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
            AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_1).build();

            Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
            /*smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                    .withStringValue("LUCICCX") //The sender ID shown on the device.
                    .withDataType("String"));
            smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue()
                    .withStringValue("0.50") //Sets the max price to 0.50 USD.
                    .withDataType("Number"));*/
            /*smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                    //.withStringValue("Promotional") //Sets the type to promotional.
                    .withStringValue("Transactional") //Sets the type to Transactional.
                    .withDataType("String"));
            */

            codigoPais = Util.getCodigoPais(sqlQueryExecutor, amazonSMSRequest.getPhone());

            PublishResult result = snsClient.publish(new PublishRequest()
                    .withMessage(amazonSMSRequest.getMessage())
                    .withPhoneNumber("+"+codigoPais+"1".concat(amazonSMSRequest.getPhone()))
                    .withMessageAttributes(smsAttributes));

            response.setErrorMessage(result.getMessageId());

        }catch (Exception e) {
            response.setStatus("-1");
            response.setErrorMessage(e.getMessage());
        }

        return  response;
    }
}
