package com.btcc.javafx;

import com.btcc.GenAccountString;
import com.btcc.MessageProvider;
import com.btcc.fix.FixApplication;
import com.btcc.fix.message.AccountInfoRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderStatusRequest;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by zhenning on 16/1/28.
 */
public class MainController {

    private @FXML TextField textFieldGetMarketDataSymbol;
    private @FXML TextField textFieldGetMarketDataMarketDepth;
    private @FXML CheckBox checkBoxGetMarketDataBid;
    private @FXML CheckBox checkBoxGetMarketDataAsk;
    private @FXML CheckBox checkBoxGetMarketDataLast;
    private @FXML CheckBox checkBoxGetMarketDataOpen;
    private @FXML CheckBox checkBoxGetMarketDataClose;
    private @FXML CheckBox checkBoxGetMarketDataHigh;
    private @FXML CheckBox checkBoxGetMarketDataLow;
    private @FXML CheckBox checkBoxGetMarketDataVolume;

    private @FXML TextArea textAreaLog;
    private @FXML TextField textFieldPlaceOrderSymbol;
    private @FXML TextField textFieldPlaceOrderPrice;
    private @FXML TextField textFieldPlaceOrderAmount;
    private @FXML
    ChoiceBox<String> choiceBoxPlaceOrderSide;
    private @FXML
    ChoiceBox<String> choiceBoxPlaceOrderOrderType;
    private @FXML CheckBox checkBoxPlaceOrderTimeInForce;

    private @FXML TextField textFieldCancelOrderSymbol;
    private @FXML TextField textFieldCancelOrderOrderId;

    private @FXML TextField textFieldGetOrderSymbol;
    private @FXML TextField textFieldGetOrderOrderId;

    private @FXML Tab tabGetOrders;
    private @FXML TextField textFieldGetOrdersSymbol;
    private @FXML TextField textFieldGetOrdersStartTime;
    private @FXML TextField textFieldGetOrdersEndTime;
    private @FXML CheckBox checkBoxGetOrdersStatusNew;
    private @FXML CheckBox checkBoxGetOrdersStatusPartiallyFilled;
    private @FXML CheckBox checkBoxGetOrdersStatusFilled;
    private @FXML CheckBox checkBoxGetOrdersStatusDoneForDay;
    private @FXML CheckBox checkBoxGetOrdersStatusCanceled;
    private @FXML CheckBox checkBoxGetOrdersStatusPendingCancel;
    private @FXML CheckBox checkBoxGetOrdersStatusPendingNew;
    private @FXML CheckBox checkBoxGetOrdersStatusDoneBySystem;
    private @FXML CheckBox checkBoxGetOrdersStatusCanceledBySystem;

    SessionSettings quickfixSettings;
    String AccessKey;
    String SecretKey;

    FixApplication fixApplication;

    public void initialize() {
        initConfig();

        initFixApplication();

        initPlaceOrderTab();

        textAreaLog.setWrapText(true);
        textAreaLog.setEditable(false);

        tabGetOrders.setOnSelectionChanged(event->{
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateNow = new Date();
            Date date1DayBefore = new Date(dateNow.getTime() - 24 * 60 * 60 * 1000);
            textFieldGetOrdersStartTime.setText(dateFormat.format(date1DayBefore));
            textFieldGetOrdersEndTime.setText(dateFormat.format(dateNow));
        });
    }

    private void initFixApplication() {

        try(InputStream inputStream = new FileInputStream("quickfix-client.properties"))
        {
            fixApplication = new FixApplication(msg-> Platform.runLater(()->{
                textAreaLog.appendText(msg);
                textAreaLog.appendText("\n");
                textAreaLog.appendText("\n");
            }));

            SessionSettings settings = new SessionSettings(inputStream);
            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            SocketInitiator initiator = new SocketInitiator(fixApplication, storeFactory, settings, logFactory, messageFactory);
            initiator.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPlaceOrderTab()
    {
        this.choiceBoxPlaceOrderSide.getItems().add("BUY");
        this.choiceBoxPlaceOrderSide.getItems().add("SELL");
        this.choiceBoxPlaceOrderSide.getSelectionModel().select(0);

        this.choiceBoxPlaceOrderOrderType.getItems().add("LIMIT");
        this.choiceBoxPlaceOrderOrderType.getItems().add("MARKET");
        this.choiceBoxPlaceOrderOrderType.getItems().add("STOP");
        this.choiceBoxPlaceOrderOrderType.getSelectionModel().select(0);
    }

    private void initConfig() {
        File configFile = new File("quickfix-client.properties");
        System.out.println("configPath is " + configFile.getAbsolutePath());
        if(!configFile.exists())
        {
            System.out.println("configPath is not exists");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No config file");
            alert.setContentText(String.format("Can't find %s !", configFile.getAbsolutePath()));

            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> System.exit(-1));
        }else {
            try(InputStream inputStreamsSessionSettings = new FileInputStream(configFile);
                InputStream inputStreamsKeys = new FileInputStream(configFile))
            {
                quickfixSettings = new SessionSettings(inputStreamsSessionSettings);

                Properties keysProperties = new Properties();
                keysProperties.load(inputStreamsKeys);
                AccessKey = keysProperties.getProperty("AccessKey");
                SecretKey = keysProperties.getProperty("SecretKey");
                String SenderCompID = keysProperties.getProperty("SenderCompID");

                System.out.println("AccessKey: " + AccessKey + " SecretKey: " + SecretKey + " SenderCompID : " + SenderCompID);
                if(AccessKey == null || AccessKey.isEmpty() || SecretKey == null || SecretKey.isEmpty() || SenderCompID == null || SenderCompID.isEmpty())
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText("No AccessKey SecretKey SenderCompID value");
                    alert.setContentText("Please set AccessKey SecretKey SenderCompID values !");

                    alert.showAndWait()
                            .filter(response -> response == ButtonType.OK)
                            .ifPresent(response -> System.exit(-1));
                }
            } catch (ConfigError | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGetMarketData(ActionEvent actionEvent) {
        String symbol = textFieldGetMarketDataSymbol.getText();
        int marketDepth = Integer.parseInt(textFieldGetMarketDataMarketDepth.getText());

        ArrayList<Character> mdtypes = new ArrayList<>(8);
        if(checkBoxGetMarketDataAsk.isSelected())
        {
            mdtypes.add(MDEntryType.OFFER);
        }

        if(checkBoxGetMarketDataBid.isSelected())
        {
            mdtypes.add(MDEntryType.BID);
        }

        if(checkBoxGetMarketDataLast.isSelected())
        {
            mdtypes.add(MDEntryType.TRADE);
        }

        if(checkBoxGetMarketDataOpen.isSelected())
        {
            mdtypes.add(MDEntryType.OPENING_PRICE);
        }

        if(checkBoxGetMarketDataClose.isSelected())
        {
            mdtypes.add(MDEntryType.CLOSING_PRICE);
        }

        if(checkBoxGetMarketDataHigh.isSelected())
        {
            mdtypes.add(MDEntryType.TRADING_SESSION_HIGH_PRICE);
        }

        if(checkBoxGetMarketDataLow.isSelected())
        {
            mdtypes.add(MDEntryType.TRADING_SESSION_LOW_PRICE);
        }

        if(checkBoxGetMarketDataVolume.isSelected())
        {
            mdtypes.add(MDEntryType.TRADE_VOLUME);
        }

        if(!symbol.isEmpty() && marketDepth > 0 && !mdtypes.isEmpty() && fixApplication != null)
        {
            MarketDataRequest message = MessageProvider.createMarketDataRequest(symbol, SubscriptionRequestType.SNAPSHOT, UUID.randomUUID().toString(), marketDepth, mdtypes);
            fixApplication.sendMessage(message);
        }
    }

    public void sendGetAccountInfo(ActionEvent actionEvent) {
        try {
            if(fixApplication != null)
            {
                String account = new GenAccountString().getAccountString(AccessKey, SecretKey);
                String accReqID = UUID.randomUUID().toString();
                AccountInfoRequest message = MessageProvider.createAccountInfoRequest(account, accReqID);
                fixApplication.sendMessage(message);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void sendPlaceOrder(ActionEvent actionEvent) {

        try {
            if(fixApplication != null) {
                char side = Side.BUY;
                String sideString = this.choiceBoxPlaceOrderSide.getValue();
                if (sideString.equals("SELL")) {
                    side = Side.SELL;
                }

                char ordertype = 0;
                String orderTypeString = choiceBoxPlaceOrderOrderType.getValue();
                switch (orderTypeString) {
                    case "LIMIT":
                        break;
                    case "MARKET":
                        ordertype = OrdType.MARKET;
                        break;
                    case "STOP":
                        ordertype = OrdType.STOP;
                        break;
                    default:
                        ordertype = OrdType.LIMIT;
                }

                String symbol = textFieldPlaceOrderSymbol.getText();
                String priceString = textFieldPlaceOrderPrice.getText();
                double price = Double.parseDouble(priceString);

                String amountString = textFieldPlaceOrderAmount.getText();
                double amount = Double.parseDouble(amountString);

                boolean timeInForceSelected = checkBoxPlaceOrderTimeInForce.isSelected();
                char timeInForce = timeInForceSelected ? TimeInForce.DAY : TimeInForce.GOOD_TILL_CANCEL;

                String account = new GenAccountString().getAccountString(AccessKey, SecretKey);
                String clOrdID = UUID.randomUUID().toString();
                Message message = MessageProvider.createNewOrderSingle(account, clOrdID, side, ordertype, price, amount, symbol, timeInForce);
                fixApplication.sendMessage(message);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    public void sendCancelOrder(ActionEvent actionEvent) {
        try {
            if(fixApplication != null) {
                String account = new GenAccountString().getAccountString(AccessKey, SecretKey);
                String clOrdID = UUID.randomUUID().toString();

                String symbol = textFieldCancelOrderSymbol.getText();
                String orderId = textFieldCancelOrderOrderId.getText();

                OrderCancelRequest message = MessageProvider.createOrderCancelRequest(account, clOrdID, symbol, orderId);
                fixApplication.sendMessage(message);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void sendGetOrder(ActionEvent actionEvent) {
        try {
            if(fixApplication != null) {
                String account = new GenAccountString().getAccountString(AccessKey, SecretKey);
                String clOrdID = UUID.randomUUID().toString();

                String symbol = textFieldGetOrderSymbol.getText();
                String orderId = textFieldGetOrderOrderId.getText();

                OrderStatusRequest message = MessageProvider.createOrderStatusRequest(account, symbol, orderId, clOrdID);
                fixApplication.sendMessage(message);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void sendGetOrders(ActionEvent actionEvent) {
        try {
            if(fixApplication != null) {
                String account = new GenAccountString().getAccountString(AccessKey, SecretKey);
                String reqID = UUID.randomUUID().toString();

                String symbol = textFieldGetOrdersSymbol.getText();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startDateTime = dateFormat.parse(textFieldGetOrdersStartTime.getText());
                Date endDateTime = dateFormat.parse(textFieldGetOrdersEndTime.getText());

                ArrayList<Character> orderStatuses = new ArrayList<>(10);
                if(checkBoxGetOrdersStatusNew.isSelected())
                {
                    orderStatuses.add(OrdStatus.NEW);
                }

                if(checkBoxGetOrdersStatusPartiallyFilled.isSelected())
                {
                    orderStatuses.add(OrdStatus.PARTIALLY_FILLED);
                }

                if(checkBoxGetOrdersStatusFilled.isSelected())
                {
                    orderStatuses.add(OrdStatus.FILLED);
                }

                if(checkBoxGetOrdersStatusDoneForDay.isSelected())
                {
                    orderStatuses.add(OrdStatus.DONE_FOR_DAY);
                }

                if(checkBoxGetOrdersStatusCanceled.isSelected())
                {
                    orderStatuses.add(OrdStatus.CANCELED);
                }

                if(checkBoxGetOrdersStatusPendingCancel.isSelected())
                {
                    orderStatuses.add(OrdStatus.PENDING_CANCEL);
                }

                if(checkBoxGetOrdersStatusPendingNew.isSelected())
                {
                    orderStatuses.add(OrdStatus.PENDING_NEW);
                }

                if(checkBoxGetOrdersStatusDoneBySystem.isSelected())
                {
                    orderStatuses.add('S');
                }

                if(checkBoxGetOrdersStatusCanceledBySystem.isSelected())
                {
                    orderStatuses.add('G');
                }

                quickfix.fix44.Message message = MessageProvider.createOrderMassStatus2Request(account, symbol, reqID, startDateTime.getTime(), endDateTime.getTime(), orderStatuses);
                fixApplication.sendMessage(message);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | DateTimeParseException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void clearLogs(ActionEvent actionEvent) {
        textAreaLog.clear();
    }
}
