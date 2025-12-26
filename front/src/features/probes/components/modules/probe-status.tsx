import ProbeStatusEnum from "@/features/probes/enums/probe-status.enum.ts";

export default function ProbeStatus({ status }: { status: string }) {
  let pingColor = "";
  let dotColor = "";

  switch (status) {
    case ProbeStatusEnum.SUCCESS:
      pingColor = "bg-green-400";
      dotColor = "bg-green-500";
      break;

    case ProbeStatusEnum.WARNING:
      pingColor = "bg-orange-400";
      dotColor = "bg-orange-500";
      break;

    default:
      pingColor = "bg-red-400";
      dotColor = "bg-red-500";
      break;
  }

  return (
    <div>
      <span className="relative flex size-8">
        <span
          className={`absolute inline-flex h-full w-full animate-ping rounded-full ${pingColor} opacity-75`}
        />
        <span
          className={`relative inline-flex size-8 rounded-full ${dotColor}`}
        />
      </span>
    </div>
  );
}
