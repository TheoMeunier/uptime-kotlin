import { useTranslation } from 'react-i18next';
import { DialogFooter, DialogHeader, DialogTitle } from '@/components/atoms/dialog.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import { Button } from '@/components/atoms/button.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import FormMultiSelect from '@/components/molecules/forms/form-select-multiple.tsx';
import { useStatusPages } from '@/features/status-pages/hooks/useStatusPages.ts';
import useAnnouncementForm, {
	ANNOUNCEMENT_BODY_MAX,
	type AnnouncementFormValues,
} from '@/features/announcements/hooks/useAnnouncementForm.ts';
import { AnnouncementSeverityEnum } from '@/features/announcements/schemas/announcement.schema.ts';

interface AnnouncementFormProps {
	mode: 'create' | 'update';
	defaultValues?: Partial<AnnouncementFormValues>;
	onSubmit: (values: AnnouncementFormValues) => void;
	isLoading?: boolean;
}

export default function AnnouncementForm({ mode, defaultValues = {}, onSubmit, isLoading }: AnnouncementFormProps) {
	const { t } = useTranslation();
	const { form, errors } = useAnnouncementForm(defaultValues);
	const { data: pages } = useStatusPages();
	const bodyLength = form.watch('body')?.length ?? 0;

	const handleSubmit = (event: React.FormEvent) => {
		event.preventDefault();
		event.stopPropagation();
		form.handleSubmit(onSubmit)(event);
	};

	return (
		<form onSubmit={handleSubmit}>
			<DialogHeader>
				<DialogTitle>{t(mode === 'create' ? 'announcements.title.create' : 'announcements.title.update')}</DialogTitle>
			</DialogHeader>

			<FieldGroup className="mt-4">
				<Field>
					<FieldLabel htmlFor="announcement-title">{t('announcements.form.title')}</FieldLabel>
					<Input
						id="announcement-title"
						maxLength={255}
						placeholder={t('announcements.form.title_placeholder')}
						aria-invalid={Boolean(errors.title)}
						{...form.register('title')}
					/>
					<FieldError>{errors.title && t('announcements.validation.title')}</FieldError>
				</Field>

				<Field>
					<FieldLabel htmlFor="announcement-severity">{t('announcements.form.severity')}</FieldLabel>
					<FormSelect
						form={form}
						name="severity"
						options={AnnouncementSeverityEnum.map((value) => ({
							value,
							label: t(`announcements.severity.${value}`),
						}))}
					/>
				</Field>

				<Field>
					<FieldLabel htmlFor="announcement-body">{t('announcements.form.body')}</FieldLabel>
					<Textarea
						id="announcement-body"
						rows={5}
						maxLength={ANNOUNCEMENT_BODY_MAX}
						placeholder={t('announcements.form.body_placeholder')}
						{...form.register('body')}
					/>
					<FieldDescription className="tabular flex justify-between gap-4">
						<span>{t('announcements.form.body_description')}</span>
						<span className="shrink-0">
							{bodyLength} / {ANNOUNCEMENT_BODY_MAX}
						</span>
					</FieldDescription>
				</Field>

				<div className="grid gap-4 sm:grid-cols-2">
					<Field>
						<FieldLabel htmlFor="announcement-starts-at">{t('announcements.form.starts_at')}</FieldLabel>
						<Input id="announcement-starts-at" type="datetime-local" {...form.register('starts_at')} />
						<FieldDescription>{t('announcements.form.starts_at_description')}</FieldDescription>
						<FieldError>{errors.starts_at && t('announcements.validation.starts_at')}</FieldError>
					</Field>

					<Field>
						<FieldLabel htmlFor="announcement-ends-at">{t('announcements.form.ends_at')}</FieldLabel>
						<Input id="announcement-ends-at" type="datetime-local" {...form.register('ends_at')} />
						<FieldDescription>{t('announcements.form.ends_at_description')}</FieldDescription>
						<FieldError>{errors.ends_at?.message}</FieldError>
					</Field>
				</div>

				<FormMultiSelect
					form={form}
					name="status_page_ids"
					label={t('announcements.form.pages')}
					description={t('announcements.form.pages_description')}
					options={(pages ?? []).map((page) => ({ label: `${page.title} (/status/${page.slug})`, value: page.id }))}
				/>

				<FormSwitch
					form={form}
					name="show_on_global"
					label={t('announcements.form.show_on_global')}
					description={t('announcements.form.show_on_global_description')}
				/>
			</FieldGroup>

			<DialogFooter className="mt-6">
				<Button type="submit" disabled={isLoading}>
					{isLoading
						? t('button.saving')
						: t(mode === 'create' ? 'announcements.actions.publish' : 'announcements.actions.save')}
				</Button>
			</DialogFooter>
		</form>
	);
}
