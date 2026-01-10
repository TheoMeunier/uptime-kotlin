import { Button } from "@/components/atoms/button.tsx";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/atoms/dialog.tsx";
import { Trash2 } from "lucide-react";
import { DialogClose } from "@radix-ui/react-dialog";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import probeService from "@/features/probes/services/probeService.ts";
import { useNavigate } from "react-router";
import { toast } from "sonner";
import { useTranslation } from "react-i18next";

export default function DeleteProbeDialogue({ probeId }: { probeId: string }) {
  const client = useQueryClient();
  const navigate = useNavigate();
  const form = useForm();
  const { t } = useTranslation();

  const mutation = useMutation({
    mutationFn: async () => {
      await probeService.deleteProbe(probeId);
    },
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ["probes"] }).then(() => {
        toast.success(t("monitors.alerts.remove"));
        navigate("/dashboard");
      });
    },
  });

  const onSubmit = () => mutation.mutate();

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="destructive">
          <Trash2 className="mr-2 h-4 w-4" />
          {t("button.actions.remove")}
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
          <DialogHeader className="items-center text-center mb-4 mt-2">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
              <Trash2 className="h-6 w-6 text-red-600" />
            </div>

            <DialogTitle className="mt-4">
              {t("monitors.title.remove")} ?
            </DialogTitle>

            <DialogDescription className="text-sm text-center text-muted-foreground mb-4">
              {t("monitors.description.remove")} ?
            </DialogDescription>
          </DialogHeader>

          <DialogFooter className="grid grid-cols-2 gap-2">
            <DialogClose asChild>
              <Button variant="outline" className="w-full">
                {t("button.cancel")}
              </Button>
            </DialogClose>

            <Button variant="destructive" className="w-full" type="submit">
              {t("button.remove", { entity: t("entity.monitor") })}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
