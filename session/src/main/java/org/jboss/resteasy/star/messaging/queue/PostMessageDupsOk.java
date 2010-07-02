package org.jboss.resteasy.star.messaging.queue;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.jboss.resteasy.star.messaging.util.LinkHeaderSupport;

import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Implements simple "create" link.  Returns 201 with Location of created resource as per HTTP
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PostMessageDupsOk extends PostMessage
{

   public void publish(HttpHeaders headers, byte[] body, boolean durable) throws Exception
   {
      ClientSession session = sessionFactory.createSession();
      try
      {
         ClientProducer producer = session.createProducer(destination);
         ClientMessage message = createHornetQMessage(headers, body, durable, session);
         producer.send(message);
         session.start();
      }
      finally
      {
         session.close();
      }

   }

   @POST
   public Response create(@Context HttpHeaders headers,
                          @Context UriInfo uriInfo,
                          byte[] body)
   {
      try
      {
         System.out.println("sending message with PostMessageDupsOk");
         publish(headers, body, defaultDurable);
      }
      catch (Exception e)
      {
         Response error = Response.serverError()
                 .entity("Problem posting message: " + e.getMessage())
                 .type("text/plain")
                 .build();
         throw new WebApplicationException(e, error);
      }
      Response.ResponseBuilder builder = Response.status(201);
      UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
      URI next = nextBuilder.build();
      LinkHeaderSupport.setLinkHeader(builder, "create-next", "create-next", next.toString(), "*/*");
      return builder.build();
   }

}