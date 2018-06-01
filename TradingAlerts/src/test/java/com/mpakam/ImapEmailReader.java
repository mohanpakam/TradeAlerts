package com.mpakam;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.*;

public class ImapEmailReader  {

  public static void main( String[] args ) throws Exception {

    Session session = Session.getDefaultInstance(new Properties( ));
    Store store = session.getStore("imaps");
    store.connect("imap.googlemail.com", 993, "trade.alerts4u@gmail.com", "TradeAlerts");
    Folder inbox = store.getFolder( "INBOX" );
    inbox.open( Folder.READ_ONLY );

    // Fetch unseen messages from inbox folder
    Message[] messages = inbox.search(
        new FlagTerm(new Flags(Flags.Flag.SEEN), false));

    // Sort messages from recent to oldest
    Arrays.sort( messages, ( m1, m2 ) -> {
      try {
        return m2.getSentDate().compareTo( m1.getSentDate() );
      } catch ( MessagingException e ) {
        throw new RuntimeException( e );
      }
    } );

    for ( Message message : messages ) {
      System.out.println( 
          "sendDate: " + message.getSentDate()
          + " subject:" + message.getSubject() );
    }
  }
}