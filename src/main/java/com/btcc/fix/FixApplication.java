package com.btcc.fix;

import com.btcc.Logger;
import quickfix.*;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.Logon;

import java.io.IOException;

public class FixApplication implements Application {

	Logger logger;
	SessionID sessionID;
	public FixApplication(Logger logger)
	{
		this.logger = logger;
	}

	public void fromAdmin(Message msg, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		logger.log(sessionID+"------ fromAdmin--------"+msg.toString());
	}

	public void fromApp(Message msg, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		String msgType = msg.getHeader().getString(35);
		if((!msgType.equals(Logon.MSGTYPE)) && (!msgType.equals(Heartbeat.MSGTYPE))){
			logger.log("        " + sessionID + "------ fromApp---------" + msg.toString());

			String[] fds = msg.toString().split("\u0001");
			for(String fd : fds)
			{
				logger.log(fd);
			}
		}
	}

	public void onCreate(SessionID sessionID) {
		try {
			//there should invoke reset()
			Session.lookupSession(sessionID).reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.log(sessionID + "------ onCreate Session-------" + sessionID);
	}


	public void onLogon(final SessionID sessionID) {
		this.sessionID = sessionID;
		logger.log(sessionID + "------ onLogon-------" + sessionID);
	}

	public void onLogout(SessionID sessionID) {
		this.sessionID = null;
		logger.log(sessionID + "------ onLogout -------" + sessionID);
	}

	public void toAdmin(Message msg, SessionID sessionID) {
		logger.log(sessionID + "------ toAdmin---------" + msg.toString());
	}

	public void toApp(Message msg, SessionID sessionID) throws DoNotSend {
		logger.log("        " + sessionID + "------ toApp-----------" + msg.toString());
	}

	public void sendMessage(Message message)
	{
		try {
			Session.sendToTarget(message, this.sessionID);
		} catch (SessionNotFound sessionNotFound) {
			sessionNotFound.printStackTrace();
		}
	}
}
