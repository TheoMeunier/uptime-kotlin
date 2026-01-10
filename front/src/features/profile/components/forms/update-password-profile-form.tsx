import { Button } from "@/components/atoms/button.tsx";
import { Input } from "@/components/atoms/input.tsx";
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/atoms/field.tsx";
import useUpdatePasswordProfile from "@/features/profile/hooks/useUpdatePasswordProfile.ts";
import { useTranslation } from "react-i18next";

export default function UpdatePasswordProfileForm() {
  const { t } = useTranslation();
  const { form, isLoading, onSubmit, errors } = useUpdatePasswordProfile();

  return (
    <div className="w-full max-w-2xl space-y-6">
      <div className="space-y-1">
        <h2 className="text-xl font-semibold">
          {t("profile.title.update_password")}
        </h2>
        <p className="text-sm text-muted-foreground">
          {t("profile.description.update_password")}
        </p>
      </div>

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="password">
              {t("profile.label.password")}
            </FieldLabel>
            <Input
              {...form.register("password")}
              id="password"
              type="password"
              required
            />
            <FieldError>{errors.password?.message}</FieldError>
          </Field>
          <Field>
            <FieldLabel htmlFor="password_confirmation">
              {t("profile.label.password_confirm")}
            </FieldLabel>
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
            {isLoading
              ? t("button.saving")
              : t("button.save", { entity: t("entity.profile") })}
          </Button>
        </div>
      </form>
    </div>
  );
}
