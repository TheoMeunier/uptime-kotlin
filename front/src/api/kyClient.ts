import ky from "ky";
import authService from "@/features/auth/services/authServices.ts";

const api = ky.extend({
    prefixUrl: "http://localhost:8080/api",
    timeout: 30000,
    headers: {
        "Content-Type": "application/json"
    },
    hooks: {
        beforeRequest: [
            request => {
                const token = authService.getAccessToken();
                console.log(token)

                if (token) {
                    request.headers.set("Authorization", `Bearer ${token}`)
                }
            }
        ],
        afterResponse: [
            async (request, _, response) => {
                if (response.status === 401) {
                    try {
                        await authService.tryRefreshToken();
                        return ky(request);
                    } catch (e) {
                        throw new Error("Error while refreshing token: " + e);
                    }
                }

                return response;
            }
        ]
    },
    retry: {
        methods: ['get', 'post'],
        limit: 3,
        statusCodes: [401],
    },
})

export default api
