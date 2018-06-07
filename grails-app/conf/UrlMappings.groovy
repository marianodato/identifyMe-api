class UrlMappings {

    static mappings = {

        "/ping" {
            controller = "ping"
            action = [GET: "index"]
        }

        "/sessions"{
            controller = "session"
            action = [POST: "doLogin", DELETE: "doLogout"]
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
            action = [POST: "createRegistrationRecord", GET: "searchRegistrationRecords"]
        }

        "/users/registration/records/$fingerprintId"{
            controller = "registrationRecord"
            action = [PUT: "modifyRegistrationRecord"]
        }

        "500"(controller: "error", action: "handleError")
        "/**"(controller: "error", action: "notFound")
    }
}