package com.jn.rest.api.endpoints;
//
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.ccp.decorators.CcpMapDecorator;
//import com.ccp.jn.sync.business.JnSyncBusinessContactUs;
//
//@CrossOrigin
//@RestController
//@RequestMapping("/contact-us")
//public class JnContactUsController {
//
//	private final JnSyncBusinessContactUs injected = new JnSyncBusinessContactUs();
//
//	@PostMapping("/")
//	@ResponseStatus(code = HttpStatus.ACCEPTED)
//	public Map<String, Object> execute(@RequestBody Map<String, Object> json) {
//		CcpMapDecorator execute = this.injected.saveContactUs(json);
//		return execute.content;
//	}
//
//	@RequestMapping("/from/{sender}/subjectType/{subjectType}", method = RequestMethod.HEAD)
//	public void verifyContactUs(@PathVariable("sender") String sender, @PathVariable("subjectType") String subjectType) {
//		
//		this.injected.verifyContactUs(sender, subjectType);
//
//	}
//}






