import type {StoreProbeSchema} from "@/features/probes/hooks/useStoreProbeForm.ts";
import api from "@/api/kyClient.ts";

const probeService = {
    async storeProbe(data: StoreProbeSchema) {
        await api.post("probes/new", {
            body: JSON.stringify(data)
        }).json();
    }
}

export default probeService