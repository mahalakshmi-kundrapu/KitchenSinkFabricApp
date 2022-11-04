package com.kony.adminconsole.preprocessor;

import java.util.HashMap;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Pre-processor to:
 * 
 * a) Decode the encoded rich text content before it gets stored at the backend database. [Introduced due to presence of
 * XSS Filter in middleware which blocks requests containing tags <>]
 * 
 * b) Sanitize rich-text content (ensure the HTML content does contain have any malicious data). [Sanitization of HTML
 * content is done using 'OWASP HTML Sanitizer Project' library files]
 * 
 * @author Mohit Khosla
 */

public class CrudLayerPreProcessor implements DataPreProcessor2 {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response,
            Result result) throws Exception {

        if (request.getParameter("rtx") != null) {
            String rtxKey = request.getParameter("rtx").toString();
            String rtxValue = request.getParameter(rtxKey);
            rtxValue = rtxValue.replaceAll("\\s", "+");
            rtxValue = CommonUtilities.decodeURI(CommonUtilities.decodeFromBase64(rtxValue));

            rtxValue = sanitizeHTML(rtxValue);

            inputMap.put(rtxKey, rtxValue);
        }

        return true;
    }

    private String sanitizeHTML(String untrustedHTML) {
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("a", "b", "blockquote", "br", "del", "div", "em", "font", "h1", "h2", "h3", "h4", "h5",
                        "h6", "i", "input", "ins", "li", "mark", "ol", "p", "small", "span", "strong", "sub", "sup",
                        "table", "td", "tr", "u", "ul")
                .allowAttributes("href").onElements("a").allowAttributes("color").onElements("font")
                .allowAttributes("id", "border", "style").onElements("table").allowAttributes("height", "width")
                .onElements("tr").allowAttributes("height", "width").onElements("td")
                .allowAttributes("class", "id", "title").globally().allowStandardUrlProtocols()
                .requireRelNofollowOnLinks().allowStyling().toFactory();

        return policy.sanitize(untrustedHTML);
    }
}
