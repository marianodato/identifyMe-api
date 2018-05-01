class UrlMappings {

    static mappings = {

        "/ping" {
            controller = "ping"
            action = [GET: "index"]
        }

        "/login"{
            controller = "login"
            action = [POST: "login"]
        }

        "/logout"{
            controller = "login"
            action = [POST: "logout"]
        }

        "/users"{
            controller = "user"
            action = [POST: "createUser"] //GET: "getAllUsers"
        }

        "/users/$id"{
            controller = "user"
            action = [GET: "getUser"] //PUT: "modifyUser", DELETE: "deleteUser"
        }

        //TODO: Sacar esto de aca y meter en el search!!!
        "/users/fingerprintStatus/pending"{
            controller = "user"
            action = [GET: "getPendingUser"]
        }

        /*"/registration/records"{
            controller = "registrationRecord"
            action = [POST: "createRegistrationRecord", PUT: "modifyRecord"]
        }*/

        "500"(controller: "error", action: "handleError")
        "/**"(controller: "error", action: "notFound")
    }
}