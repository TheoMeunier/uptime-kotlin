import ProbeStatusEnum from "@/features/probes/enums/probe-status.enum.ts";

export default function ProbeStatus({status} : {status: string}) {
    return <div>
        {status === ProbeStatusEnum.SUCCESS ? (
            <span className="relative flex size-8">
    <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-green-400 opacity-75"/>
    <span className="relative inline-flex size-8 rounded-full bg-green-500"/>
  </span>
        ) : (
            <span className="relative flex size-8">
    <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-400 opacity-75"/>
    <span className="relative inline-flex size-8 rounded-full bg-red-500"/>
  </span>
        )}
    </div>
}