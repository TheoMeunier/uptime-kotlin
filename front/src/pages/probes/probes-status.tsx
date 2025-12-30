import { useQuery } from "@tanstack/react-query";
import probeService from "@/features/probes/services/probeService.ts";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/atoms/card.tsx";
import ProbeMonitorChartBar from "@/features/probes/components/modules/probe-monitor-chart-bar.tsx";
import ProbeStatus from "@/features/probes/components/modules/probe-status.tsx";

export default function ProbesStatus() {
  const { data, isLoading } = useQuery({
    queryKey: ["probes-status"],
    queryFn: async () => {
      return probeService.getProbesStatus();
    },
    refetchInterval: 120000,
  });

  if (isLoading) return <div>Loading...</div>;

  return (
    <div>
      <section className="container mx-auto px-4 py-16">
        <h1 className="scroll-m-20 text-center text-4xl font-extrabold tracking-tight text-balance">
          Status page
        </h1>
      </section>

      <section className="w-3/4 mx-auto px-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {data.map((probe) => (
            <Card key={probe.id} className="p-4 bg-white rounded-lg shadow">
              <CardHeader className="flex justify-between items-center pt-5">
                <CardTitle className="text-xl">{probe.probe.name}</CardTitle>
                <ProbeStatus status={probe.probe.status} />
              </CardHeader>
              <CardContent>
                <ProbeMonitorChartBar
                  monitors={probe.monitors}
                  probeStatus={probe.status}
                />
              </CardContent>
            </Card>
          ))}
        </div>
      </section>
    </div>
  );
}
