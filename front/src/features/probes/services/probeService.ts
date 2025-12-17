import type {StoreProbeSchema} from "@/features/probes/hooks/useStoreProbeForm.ts";
import api from "@/api/kyClient.ts";
import probeResponseSchema, {type ProbeListItem} from "@/features/probes/schemas/probe-response.schema.ts";

const probeService = {
    async getProbes(): Promise<ProbeListItem[]> {
        const response = await api.get("probes").json();
        return probeResponseSchema.parse(response)
    },

    async storeProbe(data: StoreProbeSchema) {
        await api.post("probes/new", {
            body: JSON.stringify(data)
        }).json();
    }
}

export default probeService