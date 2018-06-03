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
            action = [POST: "createUser", GET: "searchUsers"]
        }

        "/users/$id"{
            controller = "user"
            action = [GET: "getUser", DELETE: "deleteUser", PUT: "modifyUser"]
        }

        "/users/registration/records"{
            controller = "registrationRecord"
            action = [POST: "createRegistrationRecord"]
        }

        "/users/registration/records/$fingerprintId"{
            controller = "registrationRecord"
            action = [PUT: "modifyRegistrationRecord"]
        }

        "500"(controller: "error", action: "handleError")
        "/**"(controller: "error", action: "notFound")
    }
}