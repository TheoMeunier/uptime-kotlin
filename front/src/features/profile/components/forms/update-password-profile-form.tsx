import { Button } from "@/components/atoms/button.tsx";
import { Input } from "@/components/atoms/input.tsx";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/atoms/field.tsx";
import useUpdatePasswordProfile from "@/features/profile/hooks/useUpdatePasswordProfile.ts";

export default function UpdatePasswordProfileForm() {
  const { form, isLoading, onSubmit, errors } = useUpdatePasswordProfile();

  return (
    <div className="w-full max-w-2xl space-y-6">
      <div className="space-y-1">
        <h2 className="text-xl font-semibold">Update password</h2>
        <p className="text-sm text-muted-foreground">
          Update your password regularly to help protect your account from
          unauthorized access.
        </p>
      </div>

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="email">Password</FieldLabel>
            <Input
              {...form.register("password")}
              id="password"
              type="password"
              required
            />
            <FieldError>{errors.password?.message}</FieldError>
          </Field>
          <Field>
            <FieldLabel htmlFor="email">Password confirmation</FieldLabel>
            <Input
              {...form.register("password_confirmation")}
              id="password_confirmation"
              type="password"
              required
            />
            <FieldError>{errors.password_confirmation?.message}</FieldError>
          </Field>
        </FieldGroup>

        <div>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? "Saving..." : "Save password"}
          </Button>
        </div>
      </form>
    </div>
  );
}
