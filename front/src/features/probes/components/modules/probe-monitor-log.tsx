import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/atoms/card.tsx";
import {
  Activity,
  AlertCircle,
  CheckCircle,
  Clock,
  XCircle,
} from "lucide-react";

interface Monitor {
  id: string;
  status: string;
  response_time: number;
  run_at: Date;
  message?: string;
}

export default function ProbeMonitorLog({ monitors }: { monitors: Monitor[] }) {
  return (
    <Card>
      <CardHeader className="border-b border-gray-300 pb-4">
        <CardTitle className="text-xl font-semibold">
          Probe Monitor Log Messages
        </CardTitle>
        <CardDescription>
          Recent activity from your monitoring probes
        </CardDescription>
      </CardHeader>

      <CardContent className="p-0">
        <div className="max-h-[400px] overflow-y-auto">
          {monitors?.map((monitor, index) => {
            const config = statusConfig[monitor.status] || statusConfig.PENDING;
            const Icon = config.icon;
            const runAt = new Date(monitor.run_at);

            return (
              <div
                key={monitor.id}
                className={`
                  flex items-start gap-4 p-4 
                  ${index !== monitors.length - 1 ? "border-b border-gray-300" : ""}
                  hover:bg-gray-100/50 transition-colors duration-150
                `}
              >
                <div
                  className={`
                                      flex items-center justify-center
                                      w-8 h-8 rounded-full shrink-0 mt-0.5
                                      ${config.bg} ${config.border} border
                                    `}
                >
                  <Icon className={`w-4 h-4 ${config.text}`} />
                </div>

                <div className="flex-1 min-w-0 space-y-2">
                  <div className="flex items-center gap-3 flex-wrap">
                    <span
                      className={`
                      font-semibold text-xs uppercase tracking-wide
                      ${config.text}
                    `}
                    >
                      {config.label}
                    </span>
                    <span className="text-xs text-zinc-500">
                      {runAt.toLocaleString("fr-FR", {
                        day: "2-digit",
                        month: "short",
                        hour: "2-digit",
                        minute: "2-digit",
                        second: "2-digit",
                      })}
                    </span>

                    <div
                      className={`flex items-center gap-1 px-2 py-0.5 rounded-full bg-gray-50 border`}
                    >
                      <Activity
                        className={`w-3 h-3 ${getResponseTimeColor(monitor.response_time)}`}
                      />
                      <span
                        className={`text-xs font-medium ${getResponseTimeColor(monitor.response_time)}`}
                      >
                        {monitor.response_time}ms
                      </span>
                    </div>
                  </div>

                  {/* Message */}
                  <p className="text-sm text-gray-500 leading-relaxed">
                    {monitor.message || "No message"}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

const getResponseTimeColor = (time: number) => {
  if (!time) return "text-zinc-500";
  if (time < 100) return "text-emerald-400";
  if (time < 200) return "text-amber-400";
  return "text-red-400";
};

const statusConfig: Record<StatusType, StatusConfig> = {
  SUCCESS: {
    icon: CheckCircle,
    bg: "bg-emerald-500/10",
    border: "border-emerald-500/20",
    text: "text-emerald-400",
    label: "Success",
  },
  ERROR: {
    icon: XCircle,
    bg: "bg-red-500/10",
    border: "border-red-500/20",
    text: "text-red-400",
    label: "Error",
  },
  WARNING: {
    icon: AlertCircle,
    bg: "bg-amber-500/10",
    border: "border-amber-500/20",
    text: "text-amber-400",
    label: "Warning",
  },
  PENDING: {
    icon: Clock,
    bg: "bg-blue-500/10",
    border: "border-blue-500/20",
    text: "text-blue-400",
    label: "Pending",
  },
};
