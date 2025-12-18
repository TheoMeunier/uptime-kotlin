import { CardDescription, CardTitle } from "@/components/atoms/card.tsx";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/atoms/hover-card.tsx";
import ProbeStatus from "@/features/probes/components/modules/probe-status.tsx";

interface Monitor {
  id: string;
  status: string;
  response_time: number;
  run_at: Date;
  message?: string;
}

export default function ProbeMonitorChartBar({
  monitors,
}: {
  monitors: Monitor[];
}) {
  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString("fr-FR", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("fr-FR", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  };

  const BAR_COUNT = 60;
  const bars = monitors.slice(-BAR_COUNT);
  const emptyBars = BAR_COUNT - bars.length;
  const totalBars = emptyBars + bars.length;

  return (
    <>
      <div className="flex items-center justify-between">
        <div>
          <CardTitle className="text-xl">Final hours</CardTitle>
          <CardDescription className="mt-1">
            Check every 60 seconds (1 minute)
          </CardDescription>
        </div>
        <ProbeStatus status={monitors.slice(-1).pop()!.status} />
      </div>

      <div className="w-3/4">
        <div
          className="grid gap-1 my-3 h-12 items-end"
          style={{ gridTemplateColumns: `repeat(${totalBars}, min-content)` }}
        >
          {Array.from({ length: emptyBars }).map((_, i) => (
            <div
              key={`empty-${i}`}
              className="h-12 w-2.5 rounded bg-gray-500/40"
            />
          ))}

          {bars.map((check) => (
            <HoverCard key={check.id} openDelay={100}>
              <HoverCardTrigger asChild>
                <div
                  className={`h-12 w-2.5 rounded cursor-pointer transition-all hover:scale-110 ${
                    check.status === "SUCCESS" ? "bg-green-400" : "bg-red-400"
                  }`}
                />
              </HoverCardTrigger>
              <HoverCardContent className="w-80  border-gray-300">
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h4 className="text-sm font-semibold">
                      {check.status === "SUCCESS" ? "Succès" : "Échec"}
                    </h4>
                    <span
                      className={`text-xs px-2 py-1 rounded-full ${
                        check.status === "SUCCESS"
                          ? "bg-green-500/20 text-green-400"
                          : "bg-red-500/20 text-red-400"
                      }`}
                    >
                      {check.status}
                    </span>
                  </div>
                  <div className="text-sm ">{check.message}</div>
                  {check.status === "SUCCESS" && (
                    <div className="flex items-center gap-2 text-xs text-slate-400">
                      <span className="text-blue-400 font-medium">
                        {check.response_time}ms
                      </span>
                      <span>•</span>
                      <span>Temps de réponse</span>
                    </div>
                  )}
                  <div className="pt-2 border-t border-gray-300">
                    <div className="text-xs text-slate-400">
                      {formatDate(check.run_at.toString())}
                    </div>
                    <div className="text-xs text-slate-500">
                      {formatTime(check.run_at.toString())}
                    </div>
                  </div>
                </div>
              </HoverCardContent>
            </HoverCard>
          ))}
        </div>

        <div className="flex justify-between text-xs text-slate-500 mt-1">
          <span>1 hour ago</span>
          <span>Now</span>
        </div>
      </div>
    </>
  );
}
