import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import ViewModeToggle from '@/components/molecules/view-mode-toggle.tsx';
import SlugField from '@/features/status-pages/components/forms/slug-field.tsx';
import type useStatusPageForm from '@/features/status-pages/hooks/useStatusPageForm.ts';
import { fromApiLayout, toApiLayout } from '@/features/status-pages/lib/status-layout.ts';

interface StatusPageSettingsCardProps {
	form: ReturnType<typeof useStatusPageForm>;
	isSaving: boolean;
	/* An existing page with nothing to save keeps its button disabled. */
	canSave: boolean;
	showUnsaved: boolean;
}

export default function StatusPageSettingsCard({ form, isSaving, canSave, showUnsaved }: StatusPageSettingsCardProps) {
	const { t } = useTranslation();
	const { values, errors, showErrors } = form;

	return (
		<Card className="lg:sticky lg:top-4">
			<CardHeader>
				<CardTitle>{t('status_pages.form.settings')}</CardTitle>
			</CardHeader>
			<CardContent>
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor="title">{t('status_pages.form.title')}</FieldLabel>
						<Input
							id="title"
							value={values.title}
							maxLength={255}
							placeholder={t('status_pages.form.title_placeholder')}
							aria-invalid={showErrors && Boolean(errors.title)}
							onChange={(event) => form.setTitle(event.target.value)}
						/>
						<FieldError>{showErrors ? errors.title : undefined}</FieldError>
					</Field>

					<SlugField value={values.slug} onChange={form.setSlug} error={errors.slug} showError={showErrors} />

					<Field>
						<FieldLabel htmlFor="description">{t('status_pages.form.description')}</FieldLabel>
						<Textarea
							id="description"
							value={values.description}
							placeholder={t('status_pages.form.description_placeholder')}
							onChange={(event) => form.setDescription(event.target.value)}
						/>
					</Field>

					<Field>
						<FieldLabel id="default-layout-label">{t('status_pages.form.default_layout')}</FieldLabel>
						<ViewModeToggle
							value={fromApiLayout(values.defaultLayout)}
							onChange={(layout) => form.setDefaultLayout(toApiLayout(layout))}
							aria-labelledby="default-layout-label"
							showLabels
						/>
						<FieldDescription>{t('status_pages.form.default_layout_description')}</FieldDescription>
					</Field>
				</FieldGroup>

				<Button type="submit" className="mt-6" disabled={isSaving || !canSave}>
					{isSaving ? t('button.saving') : t('status_pages.form.save')}
				</Button>

				{showUnsaved && <p className="text-status-degraded-fg mt-3 text-xs">{t('status_pages.form.unsaved')}</p>}
			</CardContent>
		</Card>
	);
}
