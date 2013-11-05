package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    static String title = "GroupDocs Java SDK Samples";

    public static Result index() {

        return ok(index.render(title));
    }

}