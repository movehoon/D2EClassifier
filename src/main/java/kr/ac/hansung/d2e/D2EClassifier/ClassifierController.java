package kr.ac.hansung.d2e.D2EClassifier;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.JsonObject;

@RestController
public class ClassifierController {
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/classifier")
    public String classifier(@RequestParam(value = "input", defaultValue = "") String input) {
        System.out.println("Input:" + input);

        JsonObject jsonObject = new JsonObject();
        try {
            String result = D2EClassifierApplication.process(input);
            jsonObject.addProperty("method", D2EClassifierApplication.getType(input));
            jsonObject.addProperty("input", D2EClassifierApplication.getInput(input));
            jsonObject.addProperty("result", result);
        }
        catch (Exception ex) {
            jsonObject.addProperty("result", "");
            System.out.println(ex.toString());
        }
        return jsonObject.toString();
    }
}
