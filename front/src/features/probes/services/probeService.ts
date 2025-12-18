import type {StoreProbeSchema} from "@/features/probes/hooks/useStoreProbeForm.ts";
import api from "@/api/kyClient.ts";
import probeResponseSchema, {
    type ProbeListItem, type ProbeShow,
} from "@/features/probes/schemas/probe-response.schema.ts";

const probeService = {
    async getProbes(): Promise<ProbeListItem[]> {
        const response = await api.get("probes").json();
        return probeResponseSchema.parse(response)
    },

    async getProbe(id: string): Promise<ProbeShow> {
        return await api.get(`probes/${id}`).json();
    },

    async storeProbe(data: StoreProbeSchema) {
        await api.post("probes/new", {
            body: JSON.stringify(data)
        }).json();
    }
}

export default probeService