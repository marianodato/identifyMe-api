package webserver

import grails.util.Environment

class SecurityFilters {

    def filters = {
        switchToHttps(controller:'*', action:'*') {
            before = {
                if (!(request.isSecure() || request.getHeader('X-Forwarded-Proto')?.toLowerCase() == 'https') && !Environment.isDevelopmentMode()) {
                    def url = "https://" + request.serverName + request.forwardURI + (request.queryString ? "?" + request.queryString : "")
                    redirect(url: url, permanent: true)
                    return false
                }
            }
        }
    }
}
