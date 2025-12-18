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

type BarItem = Monitor | { type: "pause"; id: string; timestamp: Date };

export default function ProbeMonitorChartBar({
  monitors,
  probeStatus,
}: {
  monitors: Monitor[];
  probeStatus: string;
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

  const getBarColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "bg-green-400";
      case "PAUSE":
        return "bg-gray-400";
      default:
        return "bg-red-400";
    }
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "bg-green-500/20 text-green-400";
      case "PAUSE":
        return "bg-gray-500/20 text-gray-400";
      default:
        return "bg-red-500/20 text-red-400";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "Succès";
      case "PAUSE":
        return "En pause";
      default:
        return "Échec";
    }
  };

  const BAR_COUNT = 60;
  const isPaused = probeStatus === "PAUSE";

  const buildBarsWithGaps = (): BarItem[] => {
    const allBars: BarItem[] = [];

    const sortedMonitors = [...monitors].sort(
      (a, b) => new Date(a.run_at).getTime() - new Date(b.run_at).getTime(),
    );

    for (let i = 0; i < sortedMonitors.length; i++) {
      const currentLog = sortedMonitors[i];

      if (i > 0) {
        const previousLog = sortedMonitors[i - 1];
        const previousTime = new Date(previousLog.run_at).getTime();
        const currentTime = new Date(currentLog.run_at).getTime();
        const timeDiff = currentTime - previousTime;
        const minutesDiff = Math.floor(timeDiff / (60 * 1000));

        if (minutesDiff > 1) {
          for (let j = 1; j < minutesDiff; j++) {
            const pauseTime = new Date(previousTime + j * 60 * 1000);
            allBars.push({
              type: "pause",
              id: `pause-${i}-${j}`,
              timestamp: pauseTime,
            });
          }
        }
      }

      allBars.push(currentLog);
    }

    if (isPaused && sortedMonitors.length > 0) {
      const lastLog = sortedMonitors[sortedMonitors.length - 1];
      const lastLogTime = new Date(lastLog.run_at).getTime();
      const now = new Date().getTime();
      const minutesSinceLastLog = Math.floor((now - lastLogTime) / (60 * 1000));

      if (minutesSinceLastLog > 1) {
        for (let j = 1; j < minutesSinceLastLog; j++) {
          const pauseTime = new Date(lastLogTime + j * 60 * 1000);
          allBars.push({
            type: "pause",
            id: `pause-current-${j}`,
            timestamp: pauseTime,
          });
        }
      }
    }

    return allBars;
  };

  const allBars = buildBarsWithGaps();
  const bars = allBars.slice(-BAR_COUNT);
  const emptyBars = BAR_COUNT - bars.length;

  return (
    <>
      <div className="flex items-center justify-between">
        <div>
          <CardTitle className="text-xl">Final hours</CardTitle>
          <CardDescription className="mt-1">
            Check every 60 seconds (1 minute)
          </CardDescription>
        </div>
        <ProbeStatus status={probeStatus} />
      </div>

      <div className="w-3/4">
        <div
          className="grid gap-1 my-3 h-12 items-end"
          style={{ gridTemplateColumns: `repeat(${BAR_COUNT}, min-content)` }}
        >
          {Array.from({ length: emptyBars }).map((_, i) => (
            <div
              key={`empty-${i}`}
              className="h-12 w-2.5 rounded bg-gray-500/40"
            />
          ))}

          {bars.map((item) => {
            // Si c'est une barre de pause
            if ("type" in item && item.type === "pause") {
              return (
                <HoverCard key={item.id} openDelay={100}>
                  <HoverCardTrigger asChild>
                    <div className="h-12 w-2.5 rounded cursor-pointer transition-all hover:scale-110 bg-gray-400" />
                  </HoverCardTrigger>
                  <HoverCardContent className="w-80 border-gray-300">
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <h4 className="text-sm font-semibold">Pause</h4>
                        <span className="text-xs px-2 py-1 rounded-full bg-gray-500/20 text-gray-400">
                          PAUSED
                        </span>
                      </div>
                      <div className="text-sm">
                        The monitor was on break, no checks were performed.
                      </div>
                      <div className="pt-2 border-t border-gray-300">
                        <div className="text-xs text-slate-400">
                          {formatDate(item.timestamp.toString())}
                        </div>
                        <div className="text-xs text-slate-500">
                          {formatTime(item.timestamp.toString())}
                        </div>
                      </div>
                    </div>
                  </HoverCardContent>
                </HoverCard>
              );
            }

            // Sinon c'est un log normal
            const check = item as Monitor;
            return (
              <HoverCard key={check.id} openDelay={100}>
                <HoverCardTrigger asChild>
                  <div
                    className={`h-12 w-2.5 rounded cursor-pointer transition-all hover:scale-110 ${getBarColor(
                      check.status,
                    )}`}
                  />
                </HoverCardTrigger>
                <HoverCardContent className="w-80 border-gray-300">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-semibold">
                        {getStatusLabel(check.status)}
                      </h4>
                      <span
                        className={`text-xs px-2 py-1 rounded-full ${getStatusBadgeColor(
                          check.status,
                        )}`}
                      >
                        {check.status}
                      </span>
                    </div>
                    <div className="text-sm">{check.message}</div>
                    {check.status === "SUCCESS" && (
                      <div className="flex items-center gap-2 text-xs text-slate-400">
                        <span className="text-blue-400 font-medium">
                          {check.response_time}ms
                        </span>
                        <span>•</span>
                        <span>Response time</span>
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
            );
          })}
        </div>

        <div className="flex justify-between text-xs text-slate-500 mt-1">
          <span>1 hour ago</span>
          <span>Now</span>
        </div>
      </div>
    </>
  );
}
