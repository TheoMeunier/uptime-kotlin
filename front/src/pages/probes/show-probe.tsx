import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router";
import probeService from "@/features/probes/services/probeService.ts";
import { Button } from "@/components/atoms/button.tsx";
import { Pause, Pencil } from "lucide-react";
import { ButtonGroup } from "@/components/atoms/button-group.tsx";
import { Card, CardContent } from "@/components/atoms/card.tsx";
import DeleteProbeDialogue from "@/features/probes/components/actions/delete-probe-dialogue.tsx";
import ProbeMonitorChartBar from "@/features/probes/components/modules/probe-monitor-chart-bar.tsx";
import ProbeChart from "@/features/probes/components/modules/probe-chart.tsx";
import ProbeMonitorLog from "@/features/probes/components/modules/probe-monitor-log.tsx";

export function ShowProbe() {
  const params = useParams();

  const { data, isLoading } = useQuery({
    queryKey: ["probe", params.probeId!],
    queryFn: async () => {
      return probeService.getProbe(params.probeId!);
    },
  });

  if (isLoading) return <div>Loading...</div>;

  return (
    <div className="space-y-8">
      <section className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-4">{data?.probe.name}</h1>
          <p className="text-gray-600">{data?.probe.url}</p>
        </div>

        <div>
          <ButtonGroup>
            <Button variant="outline">
              <Pause /> Pause{" "}
            </Button>
            <Button variant="outline">
              <Pencil /> Edit
            </Button>
            <DeleteProbeDialogue probeId={data!.probe.id} />
          </ButtonGroup>
        </div>
      </section>

      <section>
        <Card>
          <CardContent>
            <ProbeMonitorChartBar monitors={data!.monitors} />
          </CardContent>
        </Card>
      </section>

      <section>
        <ProbeChart monitors={data!.monitors} />
      </section>

      <section>
        <ProbeMonitorLog monitors={data!.monitors} />
      </section>
    </div>
  );
}
