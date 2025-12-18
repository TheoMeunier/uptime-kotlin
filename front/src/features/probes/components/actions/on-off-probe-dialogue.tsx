import { Button } from "@/components/atoms/button.tsx";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/atoms/dialog.tsx";
import { Pause, Play } from "lucide-react";
import { DialogClose } from "@radix-ui/react-dialog";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import probeService from "@/features/probes/services/probeService.ts";
import { useNavigate } from "react-router";

export default function OnOffMonitorProbeDialogue({
  probeId,
  enabled,
}: {
  probeId: string;
  enabled: boolean;
}) {
  const client = useQueryClient();
  const navigate = useNavigate();
  const form = useForm();

  const mutation = useMutation({
    mutationFn: async () => {
      await probeService.onoffline(probeId, !enabled);
    },
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ["probes"] }).then(() => {
        navigate("/dashboard");
      });
    },
  });

  const onSubmit = () => mutation.mutate();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="outline">
          {enabled ? (
            <>
              <Pause className="mr-2 h-4 w-4" />
              Pause
            </>
          ) : (
            <>
              <Play className="mr-2 h-4 w-4" />
              Play
            </>
          )}
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
          <DialogHeader className="items-center text-center">
            <div
              className={`flex h-12 w-12 items-center justify-center rounded-full ${enabled ? "bg-red-100" : "bg-green-100"}`}
            >
              {enabled ? (
                <Pause className="h-6 w-6 text-red-600" />
              ) : (
                <Play className="h-6 w-6 text-green-600" />
              )}
            </div>

            <DialogTitle className="my-6">
              {enabled ? "Pause" : "Play"} monitor ?
            </DialogTitle>
          </DialogHeader>

          <DialogFooter className="grid grid-cols-2 gap-2">
            <DialogClose asChild>
              <Button variant="outline" className="w-full">
                Cancel
              </Button>
            </DialogClose>

            <Button className="w-full" type="submit">
              {enabled ? "Pause" : "Play"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
