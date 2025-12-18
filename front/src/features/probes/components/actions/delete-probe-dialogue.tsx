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

export default function DeleteProbeDialogue({ probeId }: { probeId: string }) {
  const client = useQueryClient();
  const navigate = useNavigate();
  const form = useForm();

  const mutation = useMutation({
    mutationFn: async () => {
      await probeService.deleteProbe(probeId);
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
        <Button variant="destructive">
          <Trash2 className="mr-2 h-4 w-4" />
          Delete
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
          <DialogHeader className="items-center text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
              <Trash2 className="h-6 w-6 text-red-600" />
            </div>

            <DialogTitle className="mt-4">Delete monitor ?</DialogTitle>

            <DialogDescription className="text-sm text-center text-muted-foreground my-4">
              This action is irreversible. All associated data will be
              permanently deleted.
            </DialogDescription>
          </DialogHeader>

          <DialogFooter className="grid grid-cols-2 gap-2">
            <DialogClose asChild>
              <Button variant="outline" className="w-full">
                Cancel
              </Button>
            </DialogClose>

            <Button variant="destructive" className="w-full" type="submit">
              Remove
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
