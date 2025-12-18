import {useQuery} from "@tanstack/react-query";
import {useParams} from "react-router";
import probeService from "@/features/probes/services/probeService.ts";
import ProbeMonitorLog from "@/features/probes/components/probe-monitor-log.tsx";
import {Button} from "@/components/atoms/button.tsx";
import {Pause, Pencil, Trash2} from "lucide-react";
import {ButtonGroup} from "@/components/atoms/button-group.tsx";
import ProbeChart from "@/features/probes/components/probe-chart.tsx";
import {Card, CardContent} from "@/components/atoms/card.tsx";
import ProbeMonitorChartBar from "@/features/probes/components/probe-monitor-chart-bar.tsx";

export function ShowProbe() {
    const params = useParams()

    const {data, isLoading} = useQuery({
        queryKey: ['probe', params.probeId!],
        queryFn: async () => {
            return probeService.getProbe(params.probeId!)
        }
    })

    if (isLoading) return <div>Loading...</div>

    return <div className="space-y-8">
        <section className="flex items-center justify-between">
            <div>
                <h1 className="text-3xl font-bold mb-4">{data?.probe.name}</h1>
                <p className="text-gray-600">{data?.probe.url}</p>
            </div>

            <div>
                <ButtonGroup>
                    <Button variant="outline"><Pause/> Pause </Button>
                    <Button variant="outline"><Pencil/> Edit</Button>
                    <Button variant="outline"><Trash2/> Delete</Button>
                </ButtonGroup>
            </div>
        </section>

        <section>
            <Card>
                <CardContent>
                    <ProbeMonitorChartBar monitors={data!.monitors}/>
                </CardContent>
            </Card>
        </section>

        <section>
            <ProbeChart monitors={data!.monitors}/>
        </section>

        <section>
            <ProbeMonitorLog monitors={data!.monitors}/>
        </section>
    </div>
}