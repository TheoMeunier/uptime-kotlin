import {z} from "zod";
import ProbeStatusEnum from "@/features/probes/enums/probe-status.enum.ts";

const ProbeListSidebarSchema = z.array(
   z.object({
        id: z.uuid(),
        name: z.string().min(3).max(255),
        status: z.enum(ProbeStatusEnum),
        description: z.string().nullable()
    })
);

export type ProbeListItem = z.infer<typeof ProbeListSidebarSchema>[number];

export default ProbeListSidebarSchema;