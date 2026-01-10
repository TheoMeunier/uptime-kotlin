import {
  type ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/atoms/chart.tsx";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/atoms/card.tsx";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/atoms/select.tsx";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";
import * as React from "react";
import { useTranslation } from "react-i18next";

interface Monitor {
  id: string;
  status: string;
  response_time: number;
  run_at: Date;
  message?: string;
}

export default function ProbeChart({ monitors }: { monitors: Monitor[] }) {
  const { t } = useTranslation();
  const [timeRange, setTimeRange] = React.useState("1h");

  const chartConfig = {
    response_time: {
      label: "Response Time (ms)",
      color: "green",
    },
  } satisfies ChartConfig;

  const getFilteredData = () => {
    const referenceDate = new Date();
    let hoursToSubtract = 1;
    switch (timeRange) {
      case "3h":
        hoursToSubtract = 3;
        break;
      case "6h":
        hoursToSubtract = 6;
        break;
      case "24h":
        hoursToSubtract = 24;
        break;
      case "7d":
        hoursToSubtract = 24 * 7;
        break;
    }
    const startDate = new Date(referenceDate);
    startDate.setHours(startDate.getHours() - hoursToSubtract);

    const timePoints: Date[] = [];
    const intervalMinutes =
      hoursToSubtract >= 24 * 7
        ? 120
        : hoursToSubtract >= 24
          ? 60
          : hoursToSubtract >= 6
            ? 30
            : 15;

    for (
      let d = new Date(startDate);
      d <= referenceDate;
      d = new Date(d.getTime() + intervalMinutes * 60000)
    ) {
      timePoints.push(new Date(d));
    }

    const dataMap = new Map();
    monitors.forEach((item) => {
      const date = new Date(item.run_at);
      if (date >= startDate && date <= referenceDate) {
        dataMap.set(date.getTime(), item.response_time);
      }
    });

    return timePoints.map((time) => {
      let closestValue = null;
      const timeMs = time.getTime();
      const window = 5 * 60000; // 5 minutes en ms

      for (const [dataTime, value] of dataMap.entries()) {
        if (Math.abs(dataTime - timeMs) <= window) {
          closestValue = value;
          break;
        }
      }

      return {
        date: time.toISOString(),
        response_time: closestValue,
      };
    });
  };

  const filteredData = getFilteredData();

  return (
    <Card className="pt-0">
      <CardHeader className="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
        <div className="grid flex-1 gap-1">
          <CardTitle>Probe Response Time</CardTitle>
          <CardDescription>
            Showing probe response times for the last {timeRange}
          </CardDescription>
        </div>
        <Select value={timeRange} onValueChange={setTimeRange}>
          <SelectTrigger
            className="hidden w-[160px] rounded-lg sm:ml-auto sm:flex"
            aria-label="Select a value"
          >
            <SelectValue placeholder="Select time range" />
          </SelectTrigger>
          <SelectContent className="rounded-xl">
            <SelectItem value="1h" className="rounded-lg">
              {t("timeRanger.last_1_hour")}
            </SelectItem>
            <SelectItem value="3h" className="rounded-lg">
              {t("timeRanger.last_3_hours")}
            </SelectItem>
            <SelectItem value="6h" className="rounded-lg">
              {t("timeRanger.last_6_hours")}
            </SelectItem>
            <SelectItem value="24h" className="rounded-lg">
              {t("timeRanger.last_24_hours")}
            </SelectItem>
            <SelectItem value="7d" className="rounded-lg">
              {t("timeRanger.last_7_days")}
            </SelectItem>
          </SelectContent>
        </Select>
      </CardHeader>
      <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[250px] w-full"
        >
          <AreaChart data={filteredData}>
            <CartesianGrid vertical={false} strokeDasharray="3 3" />
            <XAxis
              dataKey="date"
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              minTickGap={32}
              tickFormatter={(value) =>
                new Date(value).toLocaleTimeString("en-US", {
                  hour: "2-digit",
                  minute: "2-digit",
                })
              }
            />
            <YAxis tickLine={false} axisLine={true} tickMargin={8} />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  labelFormatter={(value) =>
                    new Date(value).toLocaleTimeString("en-US", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })
                  }
                  indicator="dot"
                />
              }
            />
            <Area
              dataKey="response_time"
              type="natural"
              fill="#dcfce7"
              stroke="#22c55e"
              connectNulls={false}
            />
            <ChartLegend content={<ChartLegendContent />} />
          </AreaChart>
        </ChartContainer>
      </CardContent>
    </Card>
  );
}
